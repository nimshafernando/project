package syos.interfaces;

/**
 * Command interface for Command Pattern
 */
public interface ICommand {
    void execute();

    void undo();

    String getDescription();

    boolean canExecute();
}
