//package com.sana.circleup.navigation_fragments;
//
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Build;
//import android.os.Bundle;
//import android.text.Html;
//import android.util.Base64;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.SearchView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.firebase.ui.database.FirebaseRecyclerAdapter;
//import com.firebase.ui.database.FirebaseRecyclerOptions;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.Query;
//import com.google.firebase.database.ValueEventListener;
//import com.sana.circleup.Contacts;
//import com.sana.circleup.FindFriendsViewHolder;
//import com.sana.circleup.ProfileUserInfoActivity;
//import com.sana.circleup.R;
//import com.sana.circleup.Users;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.sana.circleup.R;
//import com.sana.circleup.Users;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class FindFriendsFragment extends Fragment {
//
//    private RecyclerView findFriendsRecyclerList;
//    private DatabaseReference usersRef;
//    private FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> adapter;
//    private String currentUserId;
//    private ArrayList<Users> allUsersList = new ArrayList<>();
//    private FindFriendsAdapter adapterrr; // Use your custom adapter
//    private FirebaseRecyclerOptions<Contacts> options;
//
//    private SearchView searchView;
//    private String query;
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_find_friends, container, false);
//
//        findFriendsRecyclerList = view.findViewById(R.id.find_friends_recycler_view);
//        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(getContext()));
//
//         searchView = view.findViewById(R.id.search_view);
//
//
//        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
//        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); //  Get the current user ID
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                @Override
//                public boolean onQueryTextSubmit(String query) {
//                    searchUsers(query); // no toLowerCase()
//                    return true;
//                }
//
//                @Override
//                public boolean onQueryTextChange(String newText) {
//                    if (newText.isEmpty()) {
//                        // Only stop listening if adapter is initialized
//                        if (adapter != null) {
//                            adapter.stopListening();
//                        }
//                        onStart(); // reload full list
//
//                        refreshData();
//
//
//                    } else {
//                        searchUsers(newText); // search functionality
//                    }
//                    return true;
//                }
//            });
//        }
//
//
//        return view;
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        FirebaseRecyclerOptions<Contacts> options =
//                new FirebaseRecyclerOptions.Builder<Contacts>()
//                        .setQuery(usersRef.orderByChild("username"), Contacts.class)
//                        .build();
//
//        adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
//            @NonNull
//            @Override
//            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.users_friends_display_layout, parent, false);
//                return new FindFriendsViewHolder(view);
//            }
//
//            @Override
//            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull Contacts model) {
//                String visit_user_id = getRef(position).getKey();
//
//                //  Append "(Me)" in bold if it's the logged-in user
//                if (visit_user_id.equals(currentUserId)) {
//                    holder.username.setText(Html.fromHtml("<b>" + model.getUsername() + " (Me)</b>"));
//                } else {
//                    holder.username.setText(model.getUsername() != null ? model.getUsername() : "Unknown");
//                }
//
//                holder.status.setText(model.getStatus() != null ? model.getStatus() : "No status");
//
//                //  Decode and Set Profile Image
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
//                    if (model.getProfileImage() != null && !model.getProfileImage().isEmpty()) {
//                        Bitmap bitmap = decodeBase64(model.getProfileImage());
//                        if (bitmap != null) {
//                            holder.profileImage.setImageBitmap(bitmap);
//                        } else {
//                            holder.profileImage.setImageResource(R.drawable.default_profile_img);
//                        }
//                    } else {
//                        holder.profileImage.setImageResource(R.drawable.default_profile_img);
//                    }
//                }
//
//                //  Handle Click to Open Profile
//                holder.itemView.setOnClickListener(view -> {
//                    Intent profileIntent = new Intent(getContext(), ProfileUserInfoActivity.class);
//                    profileIntent.putExtra("visit_user_id", visit_user_id);
//                    startActivity(profileIntent);
//                });
//            }
//
//            @Override
//            public void onDataChanged() {
//                super.onDataChanged();
//                moveCurrentUserToTop(); //  Ensure logged-in user is at the top
//            }
//        };
//
//        findFriendsRecyclerList.setAdapter(adapter);
//        adapter.startListening();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        if (adapter != null) {
//            adapter.stopListening();
//        }
//    }
//
//
//
//
//
//
//    public void refreshData() {
//        if (adapter != null) {
//            adapter.stopListening();  // Stop the current adapter to refresh data
//        }
//
//        // Re-fetch the data from Firebase and reset the adapter
//        FirebaseRecyclerOptions<Contacts> options =
//                new FirebaseRecyclerOptions.Builder<Contacts>()
//                        .setQuery(usersRef.orderByChild("username"), Contacts.class)
//                        .build();
//
//        adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
//            @NonNull
//            @Override
//            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.users_friends_display_layout, parent, false);
//                return new FindFriendsViewHolder(view);
//            }
//
//            @Override
//            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull Contacts model) {
//                String visit_user_id = getRef(position).getKey();
//
//                if (visit_user_id.equals(currentUserId)) {
//                    holder.username.setText(Html.fromHtml("<b>" + model.getUsername() + " (Me)</b>"));
//                } else {
//                    holder.username.setText(model.getUsername() != null ? model.getUsername() : "Unknown");
//                }
//
//                holder.status.setText(model.getStatus() != null ? model.getStatus() : "No status");
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
//                    if (model.getProfileImage() != null && !model.getProfileImage().isEmpty()) {
//                        Bitmap bitmap = decodeBase64(model.getProfileImage());
//                        if (bitmap != null) {
//                            holder.profileImage.setImageBitmap(bitmap);
//                        } else {
//                            holder.profileImage.setImageResource(R.drawable.default_profile_img);
//                        }
//                    } else {
//                        holder.profileImage.setImageResource(R.drawable.default_profile_img);
//                    }
//                }
//
//                holder.itemView.setOnClickListener(view -> {
//                    Intent profileIntent = new Intent(getContext(), ProfileUserInfoActivity.class);
//                    profileIntent.putExtra("visit_user_id", visit_user_id);
//                    startActivity(profileIntent);
//                });
//            }
//        };
//
//        findFriendsRecyclerList.setAdapter(adapter);
//        adapter.startListening();  // Start listening for data changes
//    }
//
//
//
//
//    //  Decode Base64 image to Bitmap
//    private Bitmap decodeBase64(String base64Str) {
//        try {
//            byte[] decodedBytes = null;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
//                decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
//            }
//            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    // Move logged-in user to the top of the list
//    private void moveCurrentUserToTop() {
//        List<Contacts> contactsList = new ArrayList<>();
//        Contacts currentUser = null;
//
//        for (int i = 0; i < adapter.getItemCount(); i++) {
//            String userId = adapter.getRef(i).getKey();
//            Contacts user = adapter.getItem(i);
//
//            if (userId.equals(currentUserId)) {
//                currentUser = user;
//            } else {
//                contactsList.add(user);
//            }
//        }
//
//        if (currentUser != null) {
//            contactsList.add(0, currentUser); // Add current user to the top
//        }
//
//        adapter.notifyDataSetChanged(); // Refresh RecyclerView
//    }
//
//
//
//    private void searchUsers(String query) {
//
//        Query searchQuery = usersRef.orderByChild("username")
//                .startAt(query)
//                .endAt(query + "\uf8ff");
//
//        FirebaseRecyclerOptions<Contacts> options =
//                new FirebaseRecyclerOptions.Builder<Contacts>()
//                        .setQuery(searchQuery, Contacts.class)
//                        .build();
//
//        adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
//            @NonNull
//            @Override
//            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.users_friends_display_layout, parent, false);
//                return new FindFriendsViewHolder(view);
//            }
//
//            @Override
//            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull Contacts model) {
//                String visit_user_id = getRef(position).getKey();
//
//                if (visit_user_id.equals(currentUserId)) {
//                    holder.username.setText(Html.fromHtml("<b>" + model.getUsername() + " (Me)</b>"));
//                } else {
//                    holder.username.setText(model.getUsername() != null ? model.getUsername() : "Unknown");
//                }
//
//                holder.status.setText(model.getStatus() != null ? model.getStatus() : "No status");
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
//                    if (model.getProfileImage() != null && !model.getProfileImage().isEmpty()) {
//                        Bitmap bitmap = decodeBase64(model.getProfileImage());
//                        if (bitmap != null) {
//                            holder.profileImage.setImageBitmap(bitmap);
//                        } else {
//                            holder.profileImage.setImageResource(R.drawable.default_profile_img);
//                        }
//                    } else {
//                        holder.profileImage.setImageResource(R.drawable.default_profile_img);
//                    }
//                }
//
//                holder.itemView.setOnClickListener(view -> {
//                    Intent profileIntent = new Intent(getContext(), ProfileUserInfoActivity.class);
//                    profileIntent.putExtra("visit_user_id", visit_user_id);
//                    startActivity(profileIntent);
//                });
//            }
//        };
//
//        // If the adapter is already initialized, stop it before setting a new one
//        if (adapter != null) {
//            adapter.stopListening();
//        }
//
//        findFriendsRecyclerList.setAdapter(adapter);
//        adapter.startListening();
//
//    }
//
//
//
//
//
//}



package com.sana.circleup.navigation_fragments; // Adjust package name if different

import android.annotation.SuppressLint; // Keep if needed
import android.content.Context; // Import Context
import android.content.Intent; // Import Intent
import android.graphics.Bitmap; // Import Bitmap
import android.graphics.BitmapFactory; // Import BitmapFactory
import android.os.Build; // Import Build
import android.os.Bundle; // Import Bundle
import android.os.Handler; // Import Handler
import android.os.Looper; // Import Looper
import android.text.Html; // Import Html
import android.text.TextUtils; // Import TextUtils
import android.util.Base64; // Import Base64
import android.util.Log; // Import Log
import android.view.LayoutInflater; // Import LayoutInflater
import android.view.View; // Import View
import android.view.ViewGroup; // Import ViewGroup
import android.widget.Button;
import android.widget.ImageView; // Import ImageView
import android.widget.TextView; // Import TextView
import android.widget.Toast; // Import Toast
import androidx.annotation.NonNull; // Import NonNull
import androidx.annotation.Nullable; // Import Nullable
import androidx.fragment.app.Fragment; // Import Fragment
import androidx.lifecycle.LiveData; // Import LiveData
import androidx.lifecycle.Observer; // Import Observer
import androidx.recyclerview.widget.LinearLayoutManager; // Import LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView; // Import RecyclerView
import android.widget.SearchView; // Keep SearchView

// Remove Firebase UI imports
// import com.firebase.ui.database.FirebaseRecyclerAdapter;
// import com.firebase.ui.database.FirebaseRecyclerOptions;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth; // Keep FirebaseAuth
import com.google.firebase.auth.FirebaseUser; // Keep FirebaseUser
import com.google.firebase.database.ChildEventListener; // Import ChildEventListener
import com.google.firebase.database.DataSnapshot; // Keep DataSnapshot
import com.google.firebase.database.DatabaseError; // Keep DatabaseError
import com.google.firebase.database.DatabaseReference; // Keep DatabaseReference
import com.google.firebase.database.FirebaseDatabase; // Keep FirebaseDatabase
import com.google.firebase.database.Query; // Keep Query for Firebase sync listener (or initial load)

import com.sana.circleup.ProfileUserInfoActivity; // Keep Intent target
import com.sana.circleup.R; // Keep your R file
import com.sana.circleup.room_db_implement.ChatDatabase;
import com.sana.circleup.room_db_implement.UserDao;
import com.sana.circleup.room_db_implement.UserEntity;

import java.util.ArrayList; // Import ArrayList
import java.util.Collections; // Keep Collections for sorting (optional if DAO orders)
import java.util.List; // Import List
import java.util.concurrent.ExecutorService; // Keep ExecutorService
import java.util.concurrent.Executors; // Use java.util.concurrent.Executors

// Remove imports for old data model and ViewHolder if they are only for old adapter
// import com.sana.circleup.Contacts; // If Contacts is only for FirebaseRecyclerAdapter
// import com.sana.circleup.Users; // If Users is only for old data model (seems to conflict with entity name)
// import com.sana.circleup.FindFriendsViewHolder; // Old ViewHolder

// Import CircleImageView if your layout uses it directly
import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriendsFragment extends Fragment {

    private static final String TAG = "FindFriendsFragment"; // Added TAG
    private View fragmentView; // Renamed for clarity in this fragment
    private RecyclerView usersRecyclerList; // Renamed from findFriendsRecyclerList
    private DatabaseReference usersRef; // Reference to /Users node
    private FirebaseAuth mAuth;
    private String currentUserId; // Current logged-in user's UID

    private TextView noUsersText; // Text view to show "Loading" or "No users found"

    // --- NEW Room DB and DAO members ---
    private ChatDatabase db; // Room Database instance
    private UserDao userDao; // DAO for UserEntity
    // LiveData for the list of users currently displayed (can be full list or search results)
    private LiveData<List<UserEntity>> currentUsersListLiveData;
    private UserAdapter userAdapter; // Standard RecyclerView Adapter for Room data
    private final List<UserEntity> usersArrayList = new ArrayList<>(); // List that holds data for the adapter

    private ExecutorService databaseExecutor; // Use the shared executor from ChatDatabase for Room ops
    // --- End NEW Room DB and DAO ---

    // --- Firebase Listener for Syncing ALL Users from Firebase to Room ---
    private ChildEventListener usersSyncChildEventListener; // Listener to sync changes from /Users

    // Keep SearchView member
    private SearchView searchView;

    // Keep Handler for posting UI updates from background threads
    private Handler mainHandler; // Use mainHandler for clarity


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called.");

        fragmentView = inflater.inflate(R.layout.fragment_find_friends, container, false);

        usersRecyclerList = fragmentView.findViewById(R.id.find_friends_recycler_view); // Ensure ID matches layout
        usersRecyclerList.setLayoutManager(new LinearLayoutManager(getContext()));

        searchView = fragmentView.findViewById(R.id.search_view); // Ensure ID matches layout
        // Assuming you have a TextView in fragment_find_friends.xml with this ID
        noUsersText = fragmentView.findViewById(R.id.noUsersText);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            Log.d(TAG, "Current User ID: " + currentUserId);
        } else {
            Log.e(TAG, "User not authenticated in FindFriendsFragment onCreateView!");
            currentUserId = null;
            // Handle case where user is not authenticated (e.g., redirect to login)
            if (getContext() != null) {
                Toast.makeText(getContext(), "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
                // Consider navigating to login activity
            }
            // Set UI state for unauthenticated user
            noUsersText.setVisibility(View.VISIBLE);
            noUsersText.setText("User not logged in.");
            usersRecyclerList.setVisibility(View.GONE);
            // Return view in an error state for unauthenticated user
            return fragmentView;
        }


        // --- Initialize Firebase DB References (Keep These) ---
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users"); // Path to all user profiles
        // --- End Initialization ---


        // --- Initialize Executors and Handler (Keep These) ---
        // chatExecutor is typically for chat-specific async ops (crypto/key management).
        // It's not strictly needed just for Find Friends list display and sync.
        // If you used it elsewhere, keep it. Removing here as it's not used in the core list logic.
        // chatExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper()); // For posting UI updates back to Main Thread

        // Progress dialog is not typically needed for just listing users from Room,
        // as Room provides fast initial load and LiveData handles updates.
        // Remove if not used elsewhere.
        // progressDialog = new ProgressDialog(getContext());
        // progressDialog.setCancelable(false);
        // --- End Initialization ---


        // --- Initialize Room DB and DAOs (NEW) ---
        // Use requireContext() in Fragment, safe because onCreateView guarantees context
        db = ChatDatabase.getInstance(requireContext()); // Get Room DB instance
        userDao = db.userDao(); // Get the DAO for UserEntity
        databaseExecutor = ChatDatabase.databaseWriteExecutor; // Use the shared DB executor for Room ops
        // --- End Initialize Room DB and DAOs ---


        // --- Setup RecyclerView Adapter (NEW) ---
        // Initialize the standard adapter. It will be empty initially, then populated by LiveData.
        // Pass a click listener implementation that opens the profile activity.
        userAdapter = new UserAdapter(usersArrayList, requireContext(), new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(UserEntity user) {
                Log.d(TAG, "User clicked: " + user.getUsername() + ", UID: " + user.getUserId());
                // Open the profile activity when a user is clicked
                Intent profileIntent = new Intent(getContext(), ProfileUserInfoActivity.class); // Keep Intent target
                profileIntent.putExtra("visit_user_id", user.getUserId()); // Pass the user's UID
                startActivity(profileIntent); // Start the activity
            }
        });
        usersRecyclerList.setAdapter(userAdapter); // Set the new standard adapter
        // --- End Setup RecyclerView Adapter ---


        // Show loading text initially while Room loads or syncs
        noUsersText.setVisibility(View.VISIBLE);
        noUsersText.setText("Loading users...");
        usersRecyclerList.setVisibility(View.GONE);


        // Removed the FirebaseRecyclerAdapter initialization and setup from onCreateView

        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called.");

        if (currentUserId == null) {
            Log.e(TAG, "Cannot start FindFriendsFragment listeners, currentUserId is null.");
            // UI state already set in onCreateView if needed
            return; // Exit if user not authenticated
        }

        // --- Step 1: Load users from Room DB using LiveData ---
        // Get LiveData for the full list of users, ordered by username (as defined in DAO query).
        // This will immediately load any existing offline data and keep UI updated with Room changes.
        currentUsersListLiveData = userDao.getAllUsers(); // Start by observing the full list

        // Observe the LiveData. getViewLifecycleOwner() ensures the observer is tied to fragment's view lifecycle.
        observeUsersLiveData(currentUsersListLiveData); // Use a helper method to handle observation logic


        // --- Step 3: Attach Firebase ChildEventListener for Syncing ---
        // This listener syncs changes from Firebase /Users *into* the Room DB.
        // Room LiveData then automatically updates the UI via the observer.
        attachUsersSyncListener(); // Call the method to attach the sync listener
        // --- End Step 3 ---

        // Step 4: Setup Search Listener (Keep Existing Logic, Modified for Room)
        setupSearchView(); // Call method to set up search listener

        // Removed FirebaseRecyclerAdapter initialization and startListening() from onStart
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called.");
        // --- Step 5: Remove Firebase Listener ---
        removeUsersSyncListener(); // Remove the ChildEventListener when the fragment is stopped
        // --- End Step 5 ---

        // Removed FirebaseRecyclerAdapter stopListening() from onStop
    }

    @Override
    public void onDestroyView() { // Use onDestroyView for fragment view cleanup
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called.");

        // Remove the current LiveData observer to prevent memory leaks
        if (currentUsersListLiveData != null) {
            currentUsersListLiveData.removeObservers(getViewLifecycleOwner()); // Use getViewLifecycleOwner()
            Log.d(TAG, "Current users LiveData observer removed.");
        }

        // If chatExecutor was used, shut it down here.
        // if (chatExecutor != null && !chatExecutor.isShutdown()) { chatExecutor.shutdownNow(); Log.d(TAG, "chatExecutor shutdown called."); }


        // Dismiss progress dialog if it's showing (if used)
        // if (progressDialog != null && progressDialog.isShowing()) { progressDialog.dismiss(); }

        // Clean up view references to help garbage collection and prevent memory leaks
        usersRecyclerList = null;
        fragmentView = null;
        noUsersText = null;
        searchView = null; // Clean up search view reference

        // adapter and usersArrayList references are typically fine as they are members

        Log.d(TAG, "onDestroyView finished.");
    }

    // Keep onDestroy for any cleanup beyond view destruction if necessary (e.g., non-view related resources)
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called.");
        // Room DB executor is managed by ChatDatabase singleton, no need to shut down here.
        // Any other long-lived resources not tied to the view should be cleaned up here.
    }

    // --- Helper method to handle LiveData observation and UI updates ---
    private void observeUsersLiveData(LiveData<List<UserEntity>> liveDataToObserve) {
        liveDataToObserve.observe(getViewLifecycleOwner(), new Observer<List<UserEntity>>() {
            @Override
            public void onChanged(List<UserEntity> userEntities) {
                Log.d(TAG, "LiveData updated with " + (userEntities != null ? userEntities.size() : 0) + " users from Room.");

                List<UserEntity> sortedList = new ArrayList<>();
                UserEntity currentUserEntity = null;

                // --- Step 2: Sort the list and move current user to top (Rewritten for standard List) ---
                if (userEntities != null) {
                    // Find the current user and add others to a temporary list
                    for (UserEntity user : userEntities) {
                        if (user.getUserId().equals(currentUserId)) { // Use fragment's currentUserId
                            currentUserEntity = user;
                        } else {
                            sortedList.add(user); // Add others to the list
                        }
                    }
                    // The DAO query already orders by username, so manual sort here might not be needed unless overriding that.
                    // If you need additional sorting after moving the current user, implement it here:
                    // Collections.sort(sortedList, (u1, u2) -> u1.getUsername().compareToIgnoreCase(u2.getUsername()));


                    // Add the current user to the top if found
                    if (currentUserEntity != null) {
                        sortedList.add(0, currentUserEntity);
                    }
                }
                // --- End Step 2 ---


                // Update the adapter's list with the new, sorted data
                usersArrayList.clear(); // Clear the list feeding the adapter
                usersArrayList.addAll(sortedList); // Add the sorted list
                userAdapter.notifyDataSetChanged(); // Notify adapter to refresh UI

                // --- Update UI visibility based on the final list size ---
                // Show "No users" message if the list is empty after sorting
                if (usersArrayList.isEmpty()) {
                    noUsersText.setVisibility(View.VISIBLE);
                    // Set text based on whether search is active or not, or just a default "No users"
                    String currentQuery = searchView != null ? searchView.getQuery().toString() : "";
                    if (!TextUtils.isEmpty(currentQuery)) {
                        noUsersText.setText("No users found matching \"" + currentQuery + "\".");
                    } else {
                        noUsersText.setText("No users found."); // Default message for empty list
                    }
                    usersRecyclerList.setVisibility(View.GONE);
                } else {
                    noUsersText.setVisibility(View.GONE);
                    usersRecyclerList.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "UI updated with " + usersArrayList.size() + " users.");
            }
        });
    }


    // --- Step 3: Method to Attach Firebase ChildEventListener for Users Sync (Keep This) ---
    // This listener watches the entire /Users node for adds, changes, and removals.
    // It then triggers updating the Room DB on a background thread.
    // WARNING: Listening to the *entire* /Users node with a ChildEventListener can be very heavy
    // and consume significant resources and data if you have many users.
    // A more scalable approach might involve periodic sync or relying on other events.
    @SuppressLint("RestrictedApi") // Keep if needed for any hidden APIs used internally by Firebase library
    private void attachUsersSyncListener() {
        if (usersRef == null) {
            Log.e(TAG, "Cannot attach users sync listener, usersRef is null.");
            return;
        }
        // Check if the listener is already attached
        if (usersSyncChildEventListener == null) {
            Log.d(TAG, "Attaching Firebase ChildEventListener for ALL Users sync to: " + usersRef.getPath());

            // Create the ChildEventListener implementation
            usersSyncChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // A new user profile is added or an existing one is found when the listener attaches
                    String userId = snapshot.getKey(); // The UID of the user

                    if (TextUtils.isEmpty(userId)) {
                        Log.w(TAG, "onChildAdded received null or empty user UID.");
                        return;
                    }
                    // No need to skip current user here, we add "(Me)" and sort later in LiveData observer.

                    Log.d(TAG, "onChildAdded triggered for user UID: " + userId);

                    // Save/update the user details in Room DB on a background thread
                    saveUserToRoomFromSnapshot(snapshot); // Pass the DataSnapshot
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // An existing user profile changed in /Users/{userId}
                    String userId = snapshot.getKey();
                    if (TextUtils.isEmpty(userId)) {
                        Log.w(TAG, "onChildChanged received null or empty user UID.");
                        return;
                    }
                    Log.d(TAG, "onChildChanged triggered for user UID: " + userId);
                    // Update the user details in Room DB on a background thread
                    saveUserToRoomFromSnapshot(snapshot); // Pass the DataSnapshot
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    // A user profile is removed from /Users/{userId}
                    String userId = snapshot.getKey();
                    if (TextUtils.isEmpty(userId)) {
                        Log.w(TAG, "onChildRemoved received null or empty user UID.");
                        return;
                    }
                    Log.d(TAG, "onChildRemoved triggered for user UID: " + userId + ". Removing from Room DB.");
                    // Remove the user from Room DB on a background thread
                    removeUserFromRoom(userId); // Pass the userId
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // A child's position changed. Not relevant for unordered lists in Room.
                    // Log.d(TAG, "onChildMoved triggered for user UID: " + snapshot.getKey());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase ALL Users Listener cancelled for " + usersRef.getPath() + ": " + error.getMessage(), error.toException());
                    // Handle error - show a message to the user on the main thread
                    if (getContext() != null) {
                        mainHandler.post(() -> Toast.makeText(getContext(), "Failed to sync users: " + error.getMessage(), Toast.LENGTH_LONG).show());
                    }
                }
            };

            // Attach the listener to the /Users node in Firebase
            usersRef.addChildEventListener(usersSyncChildEventListener);
            Log.d(TAG, "Users sync listener attached to: " + usersRef.getPath());

            // NOTE: This listener will fetch ALL users initially via onChildAdded and then listen for changes.
            // This might be slow and memory intensive if you have a very large number of users.
            // Consider querying a subset or using pagination if /Users is very large for the initial load.
        }
    }
    // --- END NEW Method to Attach Firebase Listener ---

    // --- Step 5: Method to Remove Firebase ChildEventListener (Keep This) ---
    // Remove the listener when the fragment is stopped to prevent memory leaks and unnecessary background activity.
    private void removeUsersSyncListener() {
        if (usersRef != null && usersSyncChildEventListener != null) {
            Log.d(TAG, "Removing Firebase ChildEventListener for ALL Users sync.");
            // Remove the listener from the Firebase reference
            usersRef.removeEventListener(usersSyncChildEventListener);
            usersSyncChildEventListener = null;
        }
    }
    // --- END NEW Method to Remove Firebase Listener ---


    // --- NEW Method to Save/Update User Details from Firebase Snapshot to Room DB ---
    // This method is called by the usersSyncChildEventListener when a user is added or changed in Firebase.
    private void saveUserToRoomFromSnapshot(@NonNull DataSnapshot snapshot) {
        // Ensure Room DAO and Executor are available
        if (userDao == null || databaseExecutor == null) {
            Log.e(TAG, "Cannot save user to Room: DAO or Executor is null.");
            return;
        }
        String userId = snapshot.getKey();
        if (TextUtils.isEmpty(userId)) {
            Log.w(TAG, "saveUserToRoomFromSnapshot received snapshot with empty key.");
            return;
        }

        // Extract user details from the DataSnapshot
        String username = snapshot.child("username").getValue(String.class);
        String status = snapshot.child("status").getValue(String.class);
        String profileImage = snapshot.child("profileImage").getValue(String.class); // Assuming this path based on screenshot
        String email = snapshot.child("email").getValue(String.class); // Adding email sync


        // Handle potential nulls or empty strings for required fields (optional, depends on data cleanliness)
        if (TextUtils.isEmpty(username)) username = "Unknown User";
        if (TextUtils.isEmpty(status)) status = "No Status";
        // profileImage and email can be null/empty if not set, that's typically fine.


        // Create a UserEntity object from the fetched data
        UserEntity user = new UserEntity(userId, username, status, profileImage, email); // Pass all fields

        // Save/Update the user in Room DB on a background thread using the shared executor
        databaseExecutor.execute(() -> {
            try {
                userDao.insertOrUpdateUser(user); // Insert or replace the user entry (primary key is userId)
                Log.d(TAG, "User details saved/updated in Room DB for user: " + userId);
                // LiveData observer attached in onStart will automatically pick this Room change up and update the UI
            } catch (Exception e) {
                Log.e(TAG, "Error saving/updating user " + userId + " in Room DB", e);
                // Handle Room DB errors if necessary
            }
        });
    }
    // --- END NEW Method to Save/Update User Details to Room DB ---


    // --- NEW Method to Remove User from Room DB ---
    // This method is called by the usersSyncChildEventListener when a user is removed from Firebase /Users node.
    private void removeUserFromRoom(String userId) {
        // Ensure Room DAO and Executor are available
        if (userDao == null || databaseExecutor == null) {
            Log.e(TAG, "Cannot remove user from Room DB: DAO or Executor is null.");
            return;
        }
        if (TextUtils.isEmpty(userId)) {
            Log.w(TAG, "removeUserFromRoom received empty userId.");
            return;
        }

        databaseExecutor.execute(() -> { // Use the shared database executor for Room ops
            try {
                int deletedRows = userDao.deleteUserById(userId); // Use delete by userId (primary key)
                if (deletedRows > 0) {
                    Log.d(TAG, "User removed from Room DB for user: " + userId + ". Rows deleted: " + deletedRows);
                    // LiveData observer will pick this Room change up and update UI automatically
                } else {
                    Log.w(TAG, "Attempted to remove user " + userId + " from Room DB, but it was not found.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error removing user " + userId + " from Room DB", e);
            }
        });
    }
    // --- END NEW Method to Remove User from Room DB ---


    // --- Step 4: Method to Setup Search Listener (Keep Existing Logic, Modified for Room) ---
    private void setupSearchView() {
        if (searchView == null || userDao == null || getViewLifecycleOwner() == null) { // Added check for LiveData observer
            Log.w(TAG, "SearchView, UserDao, or LifecycleOwner is null. Cannot setup search.");
            return;
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // --- Keep existing logic, but query Room DB ---
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "Search Submit: " + query);
                if (!TextUtils.isEmpty(query)) {
                    // Trigger search from Room on submit
                    searchUsersInRoom(query);
                } else {
                    // If query is empty on submit, reset to full list
                    resetSearch();
                }
                return true; // Event handled
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "Search Text Change: " + newText);
                if (TextUtils.isEmpty(newText)) {
                    // If query is empty, show the full list from Room
                    resetSearch();
                } else {
                    // Filter the list from Room as text changes
                    searchUsersInRoom(newText);
                }
                return true; // Event handled
            }
        });
        Log.d(TAG, "Search view listener setup completed.");
    }
    // --- END Step 4: Method to Setup Search Listener ---

    // --- NEW Method to Search Users in Room DB ---
    // This method switches the LiveData source to a search query.
    private void searchUsersInRoom(String query) {
        Log.d(TAG, "Searching Room DB for users matching: " + query);
        // Un-observe the current LiveData source (either full list or previous search)
        if (currentUsersListLiveData != null) {
            currentUsersListLiveData.removeObservers(getViewLifecycleOwner());
            Log.d(TAG, "Un-observed previous LiveData source.");
        }

        // Get NEW LiveData for search results from Room DAO
        currentUsersListLiveData = userDao.searchUsersByUsername(query); // Use the DAO method for searching

        // Observe the NEW search results LiveData using the shared observation logic
        observeUsersLiveData(currentUsersListLiveData); // Re-attach observer to the search results LiveData
        Log.d(TAG, "Observing search results LiveData from Room.");
    }
    // --- END NEW Method to Search Users in Room DB ---


    // --- NEW Method to Reset Search (Show Full List from Room) ---
    // This method switches the LiveData source back to the full user list.
    private void resetSearch() {
        Log.d(TAG, "Resetting search. Switching back to full user list from Room.");
        // Un-observe the current LiveData source (which is currently the search results)
        if (currentUsersListLiveData != null) {
            currentUsersListLiveData.removeObservers(getViewLifecycleOwner());
            Log.d(TAG, "Un-observed previous LiveData source (search results).");
        }

        // Get NEW LiveData for the full list from Room DAO
        currentUsersListLiveData = userDao.getAllUsers(); // Get LiveData for the full list

        // Observe the NEW full list LiveData using the shared observation logic
        // This will immediately trigger onChanged with the full data from Room.
        observeUsersLiveData(currentUsersListLiveData); // Re-attach observer to the full list LiveData
        Log.d(TAG, "Observing full list LiveData from Room after reset.");
    }
    // --- END NEW Method to Reset Search ---


    // --- Helper method to decode Base64 image to Bitmap (Keep This) ---
    private Bitmap decodeBase64(String base64Str) {
        // Use android.util.Base64 consistently
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error decoding Base64 string (invalid format)", e);
            return null; // Return null if Base64 is invalid
        } catch (Exception e) {
            Log.e(TAG, "Error decoding Base64 string", e);
            return null; // Catch any other exceptions
        }
    }


    // Removed moveCurrentUserToTop method as the sorting is done in the LiveData observer


    // --- NEW Standard RecyclerView Adapter for UserEntity (Replaces FirebaseRecyclerAdapter) ---
    // This static inner class defines the adapter for displaying UserEntity objects in the RecyclerView.
    // You can move this to a separate file if you prefer.
    public static class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

        private List<UserEntity> users; // Data list for the adapter (populated by LiveData)
        private Context context; // Context needed for Glide etc.
        private OnUserClickListener listener; // Listener for item clicks (implemented by the fragment)
        private String currentAdapterUserId; // To display "(Me)" next to current user

        // Interface for click listener (defined within the adapter)
        public interface OnUserClickListener {
            void onUserClick(UserEntity user); // Method called when an item is clicked
        }

        // Constructor for the adapter
        public UserAdapter(List<UserEntity> users, Context context, OnUserClickListener listener) {
            this.users = users; // Initial empty list
            this.context = context;
            this.listener = listener; // The fragment's implementation
            // Get current user ID here. FirebaseAuth is often accessible globally.
            this.currentAdapterUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        }

        // Method to update the data list in the adapter and notify the adapter
        // Called from the LiveData observer in the fragment whenever Room data changes.
        public void setUsers(List<UserEntity> newUsers) {
            // Using DiffUtil here would be more efficient for large lists and animations,
            // but for simplicity, we just replace the list and notify.
            this.users = newUsers; // Replace the internal list with new data from Room
            notifyDataSetChanged(); // Notify adapter that data has changed, causing UI refresh
        }


        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate the layout for a single list item view (reusing users_friends_display_layout)
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_friends_display_layout, parent, false);
            return new UserViewHolder(view); // Return a new ViewHolder instance
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            // Get the UserEntity data for the current position from the list
            UserEntity user = users.get(position);

            // Bind data from the UserEntity to the views in the ViewHolder
            // Add "(Me)" in bold for the current user
            if (user.getUserId().equals(currentAdapterUserId)) { // Compare with adapter's current user ID
                holder.username.setText(Html.fromHtml("<b>" + (user.getUsername() != null ? user.getUsername() : "Unknown") + " (Me)</b>", Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.FROM_HTML_MODE_COMPACT : 0)); // Use Html.fromHtml with flag
            } else {
                holder.username.setText(user.getUsername() != null ? user.getUsername() : "Unknown");
            }
            holder.status.setText(user.getStatus() != null ? user.getStatus() : "No status");

            // Load profile image using Glide. Supports Base64 string from Room.
            if (!TextUtils.isEmpty(user.getProfileImage())) { // Use getProfileImage() from UserEntity
                // Assuming profileImage in UserEntity is the Base64 string
                // If it's a URL, remove "data:image/jpeg;base64," prefix for Base64 loading
                // Check if the string looks like Base64 (starts with /, i, R, etc. after potential data URI prefix)
                String imageData = user.getProfileImage();
                if (imageData.startsWith("data:image")) {
                    // Glide can often handle the full data URI
                    Glide.with(context)
                            .load(imageData) // Load full data URI
                            .placeholder(R.drawable.default_profile_img) // Placeholder
                            .error(R.drawable.default_profile_img) // Error image
                            .into(holder.profileImage);
                } else {
                    // Assume it's just the Base64 part or a URL
                    // If it's Base64, add the prefix for Glide
                    String base64Prefix = "data:image/jpeg;base64,"; // Assuming JPEG
                    if (imageData.length() > 100 && (imageData.startsWith("/") || imageData.startsWith("i") || imageData.startsWith("R"))) { // Simple heuristic for Base64 start chars
                        Glide.with(context)
                                .load(base64Prefix + imageData)
                                .placeholder(R.drawable.default_profile_img)
                                .error(R.drawable.default_profile_img)
                                .into(holder.profileImage);
                    } else if (imageData.startsWith("http")) {
                        // If it's a URL, load as URL
                        Glide.with(context)
                                .load(imageData)
                                .placeholder(R.drawable.default_profile_img)
                                .error(R.drawable.default_profile_img)
                                .into(holder.profileImage);
                    }
                    else {
                        // Default if not recognized format
                        holder.profileImage.setImageResource(R.drawable.default_profile_img);
                    }
                }

            } else {
                // Set default image if profileImage string is null or empty
                holder.profileImage.setImageResource(R.drawable.default_profile_img);
            }

            // Note: Real-time online status is not easily synced for ALL users into Room/this list.
            // The UserEntity doesn't have an 'isOnline' field in this version.
            // You would need a separate mechanism to update online status in Room and trigger UI updates.
            // For now, the online icon will not reflect real-time status from Room UserEntity.
            // If your UserEntity *does* have an isOnline field synced via the listener, use it here.
            // holder.onlineIcon.setVisibility(user.isOnline() ? View.VISIBLE : View.INVISIBLE); // Example if UserEntity had isOnline field


            // >>>>>>>>>>>>>>>> SET CLICK LISTENER FOR THE ITEM >>>>>>>>>>>>>>>>>>
            // We are setting the click listener on the whole item view.
            // This matches the original FirebaseRecyclerAdapter behavior for this screen (opening profile).
            holder.itemView.setOnClickListener(v -> {
                Log.d(TAG, "User item view clicked for: " + user.getUsername());
                // Check if the listener from the fragment is set
                if (listener != null) {
                    // Call the fragment's listener method, passing the clicked UserEntity
                    // The fragment's listener will handle opening ProfileUserInfoActivity.
                    listener.onUserClick(user);
                } else {
                    Log.w(TAG, "OnUserClickListener is null. Cannot open profile from item click.");
                }
            });

            // >>>>>>>>>>>>>>>> Hide the Send Message button if it exists <<<<<<<<<<<<<<<<<<
            // Based on your layout ID and the likely function of this screen (FindFriends, not Contacts list for chatting),
            // the button R.id.send_message_btn might be intended for starting a chat directly.
            // In a Find Friends list, clicking the item usually takes you to the profile.
            // If you only want item click to open profile, hide the button.
            // If you want item click for profile AND button click for chat, you need two different listeners
            // and potentially two different listener interfaces or a combined listener.
            // For now, let's assume item click opens the profile (like original adapter) and hide the button.
//            if(holder.sendMessageBtn != null) {
//                holder.sendMessageBtn.setVisibility(View.GONE); // <<< Hide the button
//                // If you want the button to do something ELSE, attach a listener here.
//            } else {
//                Log.w(TAG, "Send message button (R.id.send_message_btn) not found in layout users_friends_display_layout.");
//            }
            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        }

        @Override
        public int getItemCount() {
            // Return the total number of items in the data list
            return users.size();
        }

        // ViewHolder class for UserEntity (should match the IDs in users_friends_display_layout)
        public static class UserViewHolder extends RecyclerView.ViewHolder {
            TextView username, status; // View for username and status
            CircleImageView profileImage; // View for profile image
            ImageView onlineIcon; // View for online status icon (if present)
            Button sendMessageBtn; // View for send message button (if present)

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                // Find the views in the item layout by their IDs
                username = itemView.findViewById(R.id.users_profile_name); // Ensure this ID is correct
                status = itemView.findViewById(R.id.users_profile_status);   // Ensure this ID is correct
                profileImage = itemView.findViewById(R.id.users_profile_image); // Ensure this ID is correct
                onlineIcon = itemView.findViewById(R.id.users_online_status); // Ensure this ID is correct
                // Find the button if it exists in the layout
                sendMessageBtn = itemView.findViewById(R.id.send_message_btn); // Ensure this ID is correct
            }
        }
    }
    // --- END NEW Standard RecyclerView Adapter ---

    // The original FindFriendsViewHolder class and old data model classes (Contacts, Users)
    // are likely no longer needed in this fragment and should be removed or commented out
    // if they were defined outside the new UserAdapter/UserEntity.
    // public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {...} // REMOVE or COMMENT OUT if duplicate
    // public class Contacts {...} // REMOVE or COMMENT OUT if duplicate
    // public class Users {...} // REMOVE or COMMENT OUT if duplicate


}