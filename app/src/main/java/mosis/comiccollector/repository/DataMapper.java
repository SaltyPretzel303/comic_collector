package mosis.comiccollector.repository;

// TODO this should be moved out of repository
// it should be in ViewModel I guess ...
public interface DataMapper<InputType, OutputType> {
    OutputType mapToViewModel(InputType input);
}
