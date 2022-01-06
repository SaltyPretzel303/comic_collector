package mosis.comiccollector.repository.impl;

import com.google.firebase.firestore.ListenerRegistration;

import mosis.comiccollector.repository.UnsubscribeProvider;

public class FirebaseUnsubProvider implements UnsubscribeProvider {

    private String itemId;

    private ListenerRegistration listenerRegistration;

    public FirebaseUnsubProvider(String itemId, ListenerRegistration lReg) {
        this.itemId = itemId;
        this.listenerRegistration = lReg;
    }

    @Override
    public void unsubscribe() {
        this.listenerRegistration.remove();
    }

    @Override
    public String getItemId() {
        return itemId;
    }
}
