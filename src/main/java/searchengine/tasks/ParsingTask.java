package searchengine.tasks;

import lombok.RequiredArgsConstructor;
import searchengine.managers.PageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class ParsingTask extends RecursiveAction {
    private final String url;
    private final PageManager manager;

    @Override
    protected void compute() {
        boolean isParsed = manager.parse(url);

        if (url.equals(manager.domain()) && !isParsed) {
            manager.statusFailed();
            manager.stop();
            return;
        } else if (!isParsed) {
            return;
        }

        manager.save(url);
        fork(manager);
    }

    private void fork(PageManager manager) {
        if (manager.isStop()) {
            return;
        }

        List<ParsingTask> tasks = new ArrayList<>();

        manager.links().forEach(link -> {
            if (manager.isNewUrl(link)) {
                ParsingTask task = new ParsingTask(link, manager);
                tasks.add(task);
                task.fork();
            }
        });
        tasks.forEach(ForkJoinTask::join);
    }
}
