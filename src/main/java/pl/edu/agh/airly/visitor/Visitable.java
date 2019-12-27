package pl.edu.agh.airly.visitor;

public interface Visitable {
    void accept(IMonitorVisitor visitor);
}
