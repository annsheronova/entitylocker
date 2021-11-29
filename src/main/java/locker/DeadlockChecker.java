package locker;

import locker.exception.DeadlockException;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public class DeadlockChecker {

    /**
     * Check whether there is cycle in IdLock threads using a graph algorithm
     * @param ids all entities lock handlers
     * @param <ID> an entity id type
     */
    public <ID> void check(Map<ID, IdLock> ids) {
        List<List<Integer>> idsGraph = createGraph(ids.values());
        if (isCyclic(idsGraph)) {
            ids.values().forEach(v -> v.setDeadlockFlag(true));
            throw new DeadlockException();
        }
    }


    /**
     * Creates graph from IdLock collection
     * @param locks IdLock collection
     * @return
     */
    private List<List<Integer>> createGraph(Collection<IdLock> locks) {
        Map<Thread, Integer> idByThread = locks.stream()
                .collect(toMap(IdLock::getCurrentThread, IdLock::getLocalId));

        List<List<Integer>> graph = new ArrayList<>();
        Map<Integer, Integer> lockIdByIndex = new HashMap<>();
        int index = 0;
        for (IdLock lock: locks) {
            int id = lock.getLocalId();
            lockIdByIndex.put(id, index++);
            graph.add(new ArrayList<>());
        }
        for (IdLock lock: locks) {
            int id = lock.getLocalId();
            Set<Thread> threads = lock.getWaitingThreads();
            for (Thread thread: threads) {
                graph.get(lockIdByIndex.get(id)).add(lockIdByIndex.get(idByThread.get(thread)));
            }
        }
        return graph;
    }

    private boolean isCyclicUtil(List<List<Integer>> adj, int i, boolean[] visited,
                                 boolean[] recStack) {
        if (recStack[i])
            return true;

        if (visited[i])
            return false;

        visited[i] = true;

        recStack[i] = true;
        List<Integer> children = adj.get(i);

        for (Integer c: children)
            if (isCyclicUtil(adj, c, visited, recStack))
                return true;

        recStack[i] = false;

        return false;
    }

    /**
     * Check whether a graph has a cycle
     * @param adj graph
     * @return
     */
    private boolean isCyclic(List<List<Integer>> adj) {
        boolean[] visited = new boolean[adj.size()];
        boolean[] recStack = new boolean[adj.size()];

        for (int i = 0; i < adj.size(); i++)
            if (isCyclicUtil(adj, i, visited, recStack))
                return true;

        return false;
    }
}
