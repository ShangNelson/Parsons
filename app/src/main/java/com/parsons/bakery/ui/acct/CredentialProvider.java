package com.parsons.bakery.ui.acct;

import android.content.Context;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CreateCredentialRequest;
import androidx.credentials.CreateCredentialResponse;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.PrepareGetCredentialResponse;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.CreateCredentialException;
import androidx.credentials.exceptions.GetCredentialException;

import java.util.concurrent.Executor;

public class CredentialProvider implements androidx.credentials.CredentialProvider {
    @Override
    public boolean isAvailableOnDevice() {
        return true;
    }

    @Override
    public void onClearCredential(@NonNull ClearCredentialStateRequest clearCredentialStateRequest, @Nullable CancellationSignal cancellationSignal, @NonNull Executor executor, @NonNull CredentialManagerCallback<Void, ClearCredentialException> credentialManagerCallback) {

    }

    @Override
    public void onCreateCredential(@NonNull Context context, @NonNull CreateCredentialRequest createCredentialRequest, @Nullable CancellationSignal cancellationSignal, @NonNull Executor executor, @NonNull CredentialManagerCallback<CreateCredentialResponse, CreateCredentialException> credentialManagerCallback) {

    }

    @Override
    public void onGetCredential(@NonNull Context context, @NonNull GetCredentialRequest getCredentialRequest, @Nullable CancellationSignal cancellationSignal, @NonNull Executor executor, @NonNull CredentialManagerCallback<GetCredentialResponse, GetCredentialException> credentialManagerCallback) {

    }

    @Override
    public void onGetCredential(@NonNull Context context, @NonNull PrepareGetCredentialResponse.PendingGetCredentialHandle pendingGetCredentialHandle, @Nullable CancellationSignal cancellationSignal, @NonNull Executor executor, @NonNull CredentialManagerCallback<GetCredentialResponse, GetCredentialException> callback) {
        androidx.credentials.CredentialProvider.super.onGetCredential(context, pendingGetCredentialHandle, cancellationSignal, executor, callback);
    }

    @Override
    public void onPrepareCredential(@NonNull GetCredentialRequest request, @Nullable CancellationSignal cancellationSignal, @NonNull Executor executor, @NonNull CredentialManagerCallback<PrepareGetCredentialResponse, GetCredentialException> callback) {
        androidx.credentials.CredentialProvider.super.onPrepareCredential(request, cancellationSignal, executor, callback);
    }
}
