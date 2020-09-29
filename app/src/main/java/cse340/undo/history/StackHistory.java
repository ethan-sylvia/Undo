package cse340.undo.history;

import android.support.annotation.NonNull;

import java.util.Deque;
import java.util.LinkedList;

import cse340.undo.actions.AbstractReversibleAction;

/**
 * Keeps a history of actions that have been done and undone using two stacks. When an item is done,
 * it is pushed onto the undo stack. When an item is undone, it is popped from the undo stack and
 * pushed to the redo stack. The number of history items is limited by the capacity.
 */
public class StackHistory implements AbstractStackHistory {
    /** Data structures for staring undo/redo events. */
    private final Deque<AbstractReversibleAction> undoStack, redoStack;

    /** Should always be true that undoStack.size() + redoStack.size() <= capacity. */
    private final int capacity;

    /**
     * Initializes empty undo/redo stacks.
     *
     * @param capacity  Maximum size of undo/redo stacks.
     * @throws IllegalStateException if capacity is not positive.
     */
    public StackHistory(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Illegal capacity: " + capacity);
        }
        this.capacity = capacity;

        undoStack = new LinkedList<>();
        redoStack = new LinkedList<>();
    }

    /**
     * Add a reversible event to the history.
     *
     * @param action    Reversible action to be added.
     */
    @Override
    public void addAction(AbstractReversibleAction action) {
        // TODO: support addAction
        // 1. If the stack is full, remove the oldest thing in it
        // 2. Add the new event to the undo stack
        // 3. Clear out the redo stack (when we do a new action we have to delete all the redo
        // actions to ensure consistency)
        if (undoStack.size() + redoStack.size() == capacity) {
            undoStack.removeFirst();
        }
        undoStack.push(action);
        for (int i = 0; i < redoStack.size(); i++) {
            redoStack.pop();
        }
    }

    /**
     * Undoes an action.
     *
     * @return null if there is nothing to undo, otherwise the action to be undone.
     */
    @Override
    public AbstractReversibleAction undo() {
        if (undoStack.size() == 0) {
            return null;
        }
        AbstractReversibleAction e = undoStack.pop();
        redoStack.push(e);
        return e;
    }

    /**
     * Redoes an action.
     *
     * @return null if there is nothing to redo, otherwise the action to be redone.
     */
    @Override
    public AbstractReversibleAction redo() {
        if (redoStack.size() == 0) {
            return null;
        }
        AbstractReversibleAction e = redoStack.pop();
        undoStack.push(e);
        return e;
    }



    /**
     * Clears the history.
     */
    @Override
    public void clear() {
        for (int i = 0; i < undoStack.size(); i++) {
            undoStack.pop();
        }
        for (int i = 0; i < redoStack.size(); i++) {
            redoStack.pop();
        }
    }

    /**
     * Is there anything that can be undone?
     *
     * @return True if can undo any actions, false otherwise.
     */
    @Override
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Is there anything that can be done?
     *
     * @return True if can redo any actions, false otherwise.
     */
    @Override
    public boolean canRedo() {return !redoStack.isEmpty();}

    @NonNull
    public String toString() {
        return  "Undo size: " + undoStack.size() + ", redo size: " + redoStack.size();
    }
}
