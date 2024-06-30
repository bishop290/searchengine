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

    @Override
    protected void compute() {
        JsoupData data = manager.parse(url);

        if (isChecksFail(manager, data)) {
            return;
        }

        manager.save(data);
        fork(manager, data);
    }

    private void fork(PageManager manager, JsoupData data) {
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

    private boolean isChecksFail(PageManager manager, JsoupData data) {
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
