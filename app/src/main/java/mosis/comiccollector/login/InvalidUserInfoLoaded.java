package mosis.comiccollector.login;

public class InvalidUserInfoLoaded extends Exception {

    private String specificMessage;

    public InvalidUserInfoLoaded() {
        this("No specific message ... ");
    }

    public InvalidUserInfoLoaded(String message) {
        this.specificMessage = message;
    }

    @Override
    public String toString() {

        String super_output = super.toString();
        return super_output + "\n" + this.specificMessage;

    }
}
