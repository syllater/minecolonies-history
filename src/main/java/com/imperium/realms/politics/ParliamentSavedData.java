package com.imperium.realms.politics;

import com.imperium.realms.colony.ColonyIdentity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Imperium-owned persistent parliament state, indexed by MineColonies identity.
 */
public final class ParliamentSavedData extends SavedData {
    public static final String DATA_NAME = "imperium_realms_parliament";
    private static final int SCHEMA_VERSION = 1;
    private static final String TAG_SESSIONS = "sessions";

    private static final Factory<ParliamentSavedData> FACTORY = new Factory<>(
            ParliamentSavedData::new,
            ParliamentSavedData::load,
            DataFixTypes.LEVEL);

    private final Map<ColonyIdentity, ParliamentSession> sessions = new LinkedHashMap<>();

    public static ParliamentSavedData get(final ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    private ParliamentSavedData() {
    }

    private static ParliamentSavedData load(
            final CompoundTag root,
            final HolderLookup.Provider registries) {
        final ParliamentSavedData data = new ParliamentSavedData();
        if (!root.contains(TAG_SESSIONS, Tag.TAG_LIST)) {
            return data;
        }

        final ListTag entries = root.getList(TAG_SESSIONS, Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            final CompoundTag entry = entries.getCompound(index);
            try {
                final ColonyIdentity identity = new ColonyIdentity(
                        entry.getString("dimension"),
                        entry.getInt("colony_id"));

                final String lawId = entry.getString("proposal_law");
                final String proposerText = entry.getString("proposer");
                final UUID proposer = proposerText.isBlank() ? null : UUID.fromString(proposerText);
                final long proposalDay = entry.getLong("proposal_day");

                final Map<UUID, Boolean> votes = new LinkedHashMap<>();
                final ListTag voteEntries = entry.getList("votes", Tag.TAG_COMPOUND);
                for (int voteIndex = 0; voteIndex < voteEntries.size(); voteIndex++) {
                    final CompoundTag vote = voteEntries.getCompound(voteIndex);
                    final UUID voter = UUID.fromString(vote.getString("voter"));
                    votes.put(voter, vote.getBoolean("yes"));
                }

                final Set<String> enacted = new LinkedHashSet<>();
                final ListTag lawEntries = entry.getList("enacted_laws", Tag.TAG_STRING);
                for (int lawIndex = 0; lawIndex < lawEntries.size(); lawIndex++) {
                    enacted.add(lawEntries.getString(lawIndex));
                }

                final ParliamentSession session = new ParliamentSession(
                        lawId.isBlank() ? null : lawId,
                        proposer,
                        proposalDay,
                        votes,
                        enacted);
                data.sessions.put(identity, session);
            } catch (IllegalArgumentException exception) {
                // Ignore malformed entries and preserve the rest of the world save.
            }
        }
        return data;
    }

    public boolean propose(
            final ColonyIdentity identity,
            final ImperialLaw law,
            final UUID proposer,
            final long gameDay) {
        final ParliamentSession session = sessions.computeIfAbsent(identity, ignored -> new ParliamentSession());
        if (!session.propose(law, proposer, gameDay)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean castVote(
            final ColonyIdentity identity,
            final UUID voter,
            final boolean inFavor) {
        final ParliamentSession session = sessions.get(identity);
        if (session == null || !session.castVote(voter, inFavor)) {
            return false;
        }
        setDirty();
        return true;
    }

    public Optional<ParliamentSession.Resolution> resolve(
            final ColonyIdentity identity,
            final long gameDay) {
        final ParliamentSession session = sessions.get(identity);
        if (session == null) {
            return Optional.empty();
        }
        final Optional<ParliamentSession.Resolution> resolution = session.resolve(gameDay);
        resolution.ifPresent(ignored -> setDirty());
        return resolution;
    }

    public ParliamentSnapshot snapshot(final ColonyIdentity identity) {
        final ParliamentSession session = sessions.get(identity);
        if (session == null) {
            return new ParliamentSnapshot(null, -1L, 0, 0, Set.of(), 0);
        }
        return new ParliamentSnapshot(
                session.proposalLawId().orElse(null),
                session.proposalGameDay(),
                session.yesVotes(),
                session.noVotes(),
                session.enactedLawIds(),
                session.votes().size());
    }

    public Collection<ColonyIdentity> colonyIdentities() {
        return new ArrayList<>(sessions.keySet());
    }

    @Override
    public CompoundTag save(final CompoundTag root, final HolderLookup.Provider registries) {
        root.putInt("schema_version", SCHEMA_VERSION);
        final ListTag entries = new ListTag();
        for (final Map.Entry<ColonyIdentity, ParliamentSession> stored : sessions.entrySet()) {
            final CompoundTag entry = new CompoundTag();
            entry.putString("dimension", stored.getKey().dimensionId());
            entry.putInt("colony_id", stored.getKey().colonyId());

            final ParliamentSession session = stored.getValue();
            entry.putString("proposal_law", session.proposalLawId().orElse(""));
            entry.putString("proposer", session.proposer().map(UUID::toString).orElse(""));
            entry.putLong("proposal_day", session.proposalGameDay());

            final ListTag votes = new ListTag();
            for (final Map.Entry<UUID, Boolean> vote : session.votes().entrySet()) {
                final CompoundTag voteEntry = new CompoundTag();
                voteEntry.putString("voter", vote.getKey().toString());
                voteEntry.putBoolean("yes", vote.getValue());
                votes.add(voteEntry);
            }
            entry.put("votes", votes);

            final ListTag enactedLaws = new ListTag();
            for (final String lawId : session.enactedLawIds()) {
                enactedLaws.add(StringTag.valueOf(lawId));
            }
            entry.put("enacted_laws", enactedLaws);
            entries.add(entry);
        }
        root.put(TAG_SESSIONS, entries);
        return root;
    }

    public record ParliamentSnapshot(
            String proposalLawId,
            long proposalGameDay,
            int yesVotes,
            int noVotes,
            Set<String> enactedLawIds,
            int votesCast) {
        public boolean hasActiveProposal() {
            return proposalLawId != null && !proposalLawId.isBlank();
        }
    }
}
