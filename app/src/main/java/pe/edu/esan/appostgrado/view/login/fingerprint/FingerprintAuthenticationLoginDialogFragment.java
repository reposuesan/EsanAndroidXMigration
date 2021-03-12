package pe.edu.esan.appostgrado.view.login.fingerprint;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import pe.edu.esan.appostgrado.R;
import pe.edu.esan.appostgrado.view.login.LoginActivity;


/**
 * Created by lventura on 24/07/17.
 */

public class FingerprintAuthenticationLoginDialogFragment extends DialogFragment implements TextView.OnEditorActionListener, FingerprintLoginUIHelper.Callback {

    private Button mCancelButton;
    private TextView descriptionTextView;

    private Stage mStage = Stage.FINGERPRINT;

    private FingerprintManager.CryptoObject mCryptoObject;
    private FingerprintLoginUIHelper mFingerprintUiHelper;
    private LoginActivity mActivity;

    private static final String LOG = FingerprintAuthenticationLoginDialogFragment.class.getSimpleName();

    private FingerprintResultsLogin callback;

    public void setFingerprintResultsLoginInterface(FingerprintResultsLogin callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getBaseContext().getString(R.string.login_huella_title));
        View v = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);

        descriptionTextView = v.findViewById(R.id.fingerprint_description);
        descriptionTextView.setText(getActivity().getBaseContext().getString(R.string.utiliza_huella_digital));

        mCancelButton = (Button) v.findViewById(R.id.cancel_button);
        mCancelButton.setText(mCancelButton.getContext().getString(R.string.cancelar_dialog_text));
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*mActivity.onCancel();*/
                callback.onCancel();
                //dismiss();
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mFingerprintUiHelper = new FingerprintLoginUIHelper(mActivity.getSystemService(FingerprintManager.class), (ImageView) v.findViewById(R.id.fingerprint_icon), (TextView) v.findViewById(R.id.fingerprint_status), this);
        }

        return v;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return (actionId == EditorInfo.IME_ACTION_GO);
    }

    @Override
    public void onAuthenticated() {
        //Callback from FingerprintUiHelper. Let the activity know that authentication was successful.
        /*mActivity.onExito(///* withFingerprint , mCryptoObject*///)
        callback.onExito();
        //dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mStage == Stage.FINGERPRINT) {
            mFingerprintUiHelper.startListening(mCryptoObject);
        }
    }

    public void setStage(Stage stage) {
        mStage = stage;
    }

    @Override
    public void onPause() {
        mFingerprintUiHelper.stopListening();
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (LoginActivity) getActivity();
    }

    /**
     * Sets the crypto object to be passed in when authenticating with fingerprint.
     */
    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }


    @Override
    public void onError(int errorId) {
        // Fingerprint is not used anymore. Stop listening for it.
        mFingerprintUiHelper.stopListening();
        /*mActivity.onErrorFingerprint();*/
        callback.onErrorFingerprint(errorId);
        //dismiss();
    }

    public enum Stage {
        FINGERPRINT,
        PASSWORD
    }

    public interface FingerprintResultsLogin{
        /*void onPurchased(Boolean withFingerprint, FingerprintManager.CryptoObject cryptopObject);*/
        void onExito();
        void onCancel();
        void onErrorFingerprint(int errorId);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onCancel();
        //dismiss();
    }
}
