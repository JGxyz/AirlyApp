package pl.edu.agh.airly.visitor;

import pl.edu.agh.airly.model.Monitor;

public interface IMonitorVisitor {
    void visit(Monitor monitor);
}
