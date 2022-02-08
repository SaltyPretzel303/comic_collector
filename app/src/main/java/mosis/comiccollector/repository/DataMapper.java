package mosis.comiccollector.repository;

public interface DataMapper<InputType, OutputType> {
    OutputType mapThis(InputType input);
}
