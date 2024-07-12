package searchengine.tasks;

import lombok.RequiredArgsConstructor;
import searchengine.managers.JsoupData;
import searchengine.managers.PageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class PageParsingTask extends RecursiveAction {
    private final String url;
    private final PageManager manager;
    private JsoupData data;
    private Map<String, Integer> lemmas;

    @Override
    protected void compute() {
        if (!parse()) {
            return;
        }
        getLemmas();
        forkForLinks();
        saveData();
    }

    public boolean parse() {
        data = manager.parse(url);
        return checks();
    }

    public void getLemmas() {
        lemmas = manager.createLemmas(data);
    }

    public void saveData() {
        manager.save(data, lemmas);
    }

    private void forkForLinks() {
        if (manager.isStop()) {
            return;
        }
        List<PageParsingTask> tasks = new ArrayList<>();

        manager.links(data).forEach(link -> {
            if (manager.isNewUrl(link)) {
                PageParsingTask task = new PageParsingTask(link, manager);
                tasks.add(task);
                task.fork();
            }
        });
        tasks.forEach(ForkJoinTask::join);
    }

    private boolean checks() {
        boolean dataIsNotValid = !data.isValid();
        if (manager.isDomain(url) && dataIsNotValid) {
            manager.statusFailed(data);
            manager.stop();
            return false;
        } else if (!manager.initTextService()) {
            String message = "Error: TextService didnt start";
            manager.statusFailed(new JsoupData(url, -1, null, message));
            manager.stop();
            return false;
        } else if (dataIsNotValid) {
            return false;
        }
        return true;
    }
}
