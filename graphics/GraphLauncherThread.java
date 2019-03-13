package graphics;

import javafx.application.Application;
import javafx.application.Platform;

class GraphLauncherThread extends Thread {

    @Override
    public void run() {
        new Thread(() -> Application.launch(GraphPlotter.class)).start();
        boolean mainIsRun = true;

        while (mainIsRun){
            try{
                ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
                int noThreads = currentGroup.activeCount();
                Thread[] lstThreads = new Thread[noThreads];
                currentGroup.enumerate(lstThreads);

                mainIsRun = false;
                for (Thread t : lstThreads){
                    if (t != null && t.getName() != null && t.getName().equals("main")){
                        mainIsRun = true;
                    }
                }
            }catch (Exception e){
                break;
            }

        }
        Platform.setImplicitExit(true);
    }
}
