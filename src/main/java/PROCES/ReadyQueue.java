package PROCES;

import java.util.LinkedList;
import java.util.Queue;

public class ReadyQueue {

    private Queue<PCB> queue = new LinkedList<>();

    public ReadyQueue(Queue<PCB> queue) {
        this.queue = queue;
    }

    public void add(PCB p){
        queue.add(p);
    }

    public PCB removeNext(){
        return queue.poll();
    }

    public boolean isEmpty(){
        return queue.isEmpty();
    }

    public void remove(PCB pcb) {
        queue.remove(pcb);
    }
}
