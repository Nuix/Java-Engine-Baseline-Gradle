package Nuix.Baseline;

import nuix.WorkerItem;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import Nuix.Baseline.SimpleAddress;
import Nuix.Baseline.SimpleCommunication;

/**
 * An example for how to configure a Java based Worker Side Script
 */
public class WSSExample implements Consumer<WorkerItem>, AutoCloseable
{

    /**
     * nuixWorkerItemCallbackInit
     * Called when processing starts
     */
    public WSSExample() throws Exception {
        //Anything sent to System.out will appear in stdout.log in the job folder.
        System.out.println("initiated");

    }

    /**
     * nuixWorkerItemCallback
     * Will be called with a single argument which is the current WorkerItem being processed
     * @param workerItem the current WorkerItem being processed
     */
    @Override
    public void accept(WorkerItem workerItem)
    {
        System.out.println("Worker Item Processed");
    }

    /**
     * nuixWorkerItemCallbackClose
     * Will be called once processing completes
     */
    @Override
    public void close() {
        System.out.println("Shut Down");
    }

}
