package com.ai.context;

import com.ai.model.ApiInteraction;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores redacted REST Assured interactions for the currently running test so
 * failure triage can include the request and response evidence captured earlier.
 */
public final class TriageEvidenceContext {
    private static final ThreadLocal<List<ApiInteraction>> INTERACTIONS =
            ThreadLocal.withInitial(ArrayList::new);

    /**
     * Prevents instantiation because this class manages per-thread evidence state.
     */
    private TriageEvidenceContext() {
    }

    /**
     * Starts a fresh interaction list for the current test thread.
     */
    public static void startTest() {
        INTERACTIONS.set(new ArrayList<>());
    }

    /**
     * Adds one captured API interaction to the current test's evidence.
     *
     * @param interaction redacted request/response evidence to retain
     */
    public static void record(ApiInteraction interaction) {
        if (interaction != null) {
            INTERACTIONS.get().add(interaction);
        }
    }

    /**
     * Returns a snapshot of the interactions captured for the current test.
     *
     * @return immutable copy of captured API interactions
     */
    public static List<ApiInteraction> interactions() {
        return List.copyOf(INTERACTIONS.get());
    }

    /**
     * Clears the current thread's captured evidence after the test finishes.
     */
    public static void clear() {
        INTERACTIONS.remove();
    }
}
