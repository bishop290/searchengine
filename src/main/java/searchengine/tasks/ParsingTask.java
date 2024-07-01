package searchengine.tasks;

import lombok.RequiredArgsConstructor;
import searchengine.managers.JsoupData;
import searchengine.managers.PageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class ParsingTask extends RecursiveAction {
    private final String url;
    private final PageManager manager;
    private JsoupData data;

    @Override
    protected void compute() {
        if (!parse()) {
            return;
        }
        forkForLinks();
    }

    public boolean parse() {
        data = manager.parse(url);
        if (isChecksFail()) {
            return false;
        }
        manager.save(data);
        return true;
    }

    private void forkForLinks() {
        if (manager.isStop()) {
            return;
        }
        List<ParsingTask> tasks = new ArrayList<>();

        manager.links(data).forEach(link -> {
            if (manager.isNewUrl(link)) {
                ParsingTask task = new ParsingTask(link, manager);
                tasks.add(task);
                task.fork();
            }
        });
        tasks.forEach(ForkJoinTask::join);
    }

    private boolean isChecksFail() {
        boolean dataIsNotValid = !data.isValid();
        if (manager.isDomain(url) && dataIsNotValid) {
            manager.statusFailed(data);
            manager.stop();
            return true;
        } else if (!manager.initTextService()) {
            String message = "Error: TextService didnt start";
            manager.statusFailed(new JsoupData(url, -1, null, message));
            manager.stop();
            return true;
        } else if (dataIsNotValid) {
            return true;
        }
        return false;
    }
}
