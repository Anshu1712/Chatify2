package com.example.chatify.contacts;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatify.R;
import com.example.chatify.adapters.ContactAdapter;
import com.example.chatify.databinding.ActivityAddContactBinding;
import com.example.chatify.model.Users;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddContact extends AppCompatActivity {

    public static final int REQUEST_READ_CONTACTS = 79;
    private static final String TAG = "AddContact";
    private ActivityAddContactBinding binding;
    private List<Users> list = new ArrayList<>();
    private ContactAdapter adapter;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private ArrayList<String> mobileArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_contact);

        binding.contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if (firebaseUser != null) {
            getContactFromPhone();
        }

        if (mobileArray != null) {
            getContactList();
        }

        binding.backArrow.setOnClickListener(v -> finish());
    }

    private void getContactFromPhone() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            mobileArray = getAllPhoneContacts();
        } else {
            requestPermission();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mobileArray = getAllPhoneContacts();
                getContactList();
            } else {
                finish();
            }
        }
    }

    @SuppressLint("Range")
    private ArrayList<String> getAllPhoneContacts() {
        ArrayList<String> phoneList = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cur != null && cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));

                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null
                    );

                    while (pCur != null && pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        // ✅ Normalize the phone number before adding
                        phoneNo = phoneNo.replaceAll("[\\s\\-()]", ""); // Remove spaces, dashes, and brackets
                        phoneNo = normalizePhoneNumber(phoneNo);         // Normalize to 10-digit format

                        if (!phoneList.contains(phoneNo)) {
                            phoneList.add(phoneNo);
                        }
                    }

                    if (pCur != null) {
                        pCur.close();
                    }
                }
            }
            cur.close();
        }
        return phoneList;
    }

    /**
     * ✅ Normalize phone number format:
     * - Remove country code (+91) if present
     * - Ensure consistent 10-digit format for matching
     */
    private String normalizePhoneNumber(String phone) {
        // Remove all non-digit characters
        phone = phone.replaceAll("[^0-9]", "");

        // Remove +91 or 91 from the start if present
        if (phone.startsWith("+91") && phone.length() > 10) {
            phone = phone.substring(3);  // Remove the first two digits (country code)
        }

        return phone;
    }

    private void getContactList() {
        firestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                list.clear();

                for (QueryDocumentSnapshot snapshots : queryDocumentSnapshots) {
                    String userID = snapshots.getString("userID");
                    String userName = snapshots.getString("username");
                    String imageUrl = snapshots.getString("imageProfile");
                    String desc = snapshots.getString("bio");
                    String phone = snapshots.getString("userPhone");

                    // ✅ Normalize Firestore phone number
                    phone = normalizePhoneNumber(phone);

                    Users user = new Users();
                    user.setUserID(userID);
                    user.setUsername(userName);
                    user.setBio(desc);
                    user.setImageProfile(imageUrl);
                    user.setUserPhone(phone);

                    // ✅ Compare normalized phone numbers
                    if (userID != null && !userID.equals(firebaseUser.getUid()) && mobileArray.contains(phone)) {
                        list.add(user);
                    }
                }

                // ✅ Populate the RecyclerView
                adapter = new ContactAdapter(list, AddContact.this);
                binding.contactsRecyclerView.setAdapter(adapter);
            }
        });
    }
}
