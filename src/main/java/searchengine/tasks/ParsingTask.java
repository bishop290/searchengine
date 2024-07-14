package searchengine.tasks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import searchengine.managers.JsoupData;
import searchengine.managers.PageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@Getter
@RequiredArgsConstructor
public class ParsingTask extends RecursiveAction {
    private final String url;
    private final PageManager manager;
    private String path;
    private JsoupData data;
    private Map<String, Integer> index;

    @Override
    protected void compute() {
        if (!parse()) {
            return;
        }
        calculateIndex();
        saveData();
        forkForLinks();
    }

    public boolean parse() {
        data = manager.parse(url);
        return checks();
    }

    public void calculateIndex() {
        index = manager.createIndex(data);
    }

    public void saveData() {
        path = manager.save(data, index);
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

    private boolean checks() {
        boolean dataIsNotValid = !data.isValid();
        if (manager.isDomain(url) && dataIsNotValid) {
            manager.statusFailed(data);
            manager.stop();
            return false;
        } else if (!manager.initTextService()) {
            String message = "Error: TextService didn't start";
            manager.statusFailed(new JsoupData(url, -1, null, message));
            manager.stop();
            return false;
        } else if (dataIsNotValid) {
            return false;
        }
        return true;
    }
}
