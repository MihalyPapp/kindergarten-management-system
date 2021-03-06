package com.mihalypapp.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mihalypapp.app.R;
import com.mihalypapp.app.activities.AddUserActivity;
import com.mihalypapp.app.activities.MainActivity;
import com.mihalypapp.app.activities.UserActivity;
import com.mihalypapp.app.models.EndlessRecyclerViewScrollListener;
import com.mihalypapp.app.models.User;
import com.mihalypapp.app.adapters.UserCardAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class ListUsersFragment extends Fragment {

    private static final String TAG = "LUSSPFragment";

    private static final int RC_OVERVIEW_USER = 11;
    private static final int RC_ADD_USER = 111;

    private ArrayList<User> userCardList = new ArrayList<>();

    private boolean refreshing = false;
    private boolean fetching = false;
    private boolean showingProgressBar = false;
    private int offset = 0;
    private String filter = "";

    private RecyclerView recyclerView;
    private UserCardAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_list_users, container, false);


        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.users);
        setHasOptionsMenu(true);
        FloatingActionButton floatingActionButton = view.findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddUserActivity.class);
                startActivityForResult(intent, RC_ADD_USER);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        adapter = new UserCardAdapter(userCardList);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (!fetching) {
                    addProgressBar();
                    fetchUsers();
                }
            }
        };

        recyclerView.addOnScrollListener(scrollListener);

        swipeContainer = view.findViewById(R.id.swipe_container);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!fetching) {
                    refreshing = true;
                    offset = 0;
                    fetchUsers();
                }
            }
        });

        adapter.setOnItemClickListener(new UserCardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                int userId = userCardList.get(position).getId();
                Intent intent = new Intent(getContext(), UserActivity.class);
                intent.putExtra(UserActivity.USER_ID, userId);
                startActivityForResult(intent, RC_OVERVIEW_USER);
            }
        });

        fetchUsers();
        return view;
    }

    private void fetchUsers() {
        fetching = true;

        JSONObject params = new JSONObject();
        try {
            params.put("role", getRole());
            params.put("offset", offset);
            params.put("quantity", 15);
            params.put("filter", filter);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest getUsersRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "users", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                Log.i(TAG, response.toString());
                                JSONArray users = response.getJSONArray("users");

                                removeProgressBar();

                                if (refreshing) {
                                    userCardList.clear();
                                    adapter.notifyDataSetChanged();
                                }

                                int i;
                                for (i = 0; i < users.length(); i++) {
                                    JSONObject user = users.getJSONObject(i);
                                    userCardList.add(new User(
                                            user.getInt("userId"),
                                            R.drawable.ic_launcher_foreground,
                                            user.getString("name"),
                                            user.getString("email")
                                    ));
                                    offset++;
                                }

                                if (!refreshing) {
                                    adapter.notifyItemRangeInserted(userCardList.size() - i, i);
                                } else {
                                    adapter.notifyDataSetChanged();
                                    scrollListener.resetState();
                                    refreshing = false;
                                }

                            } else {
                                Log.e(TAG, "getUserRequest ERROR");
                            }

                            swipeContainer.setRefreshing(false);
                            fetching = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(getUsersRequest);
    }

    private void addProgressBar() {
        showingProgressBar = true;
        userCardList.add(null);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemInserted(userCardList.size() - 1);
            }
        });
    }

    private void removeProgressBar() {
        if (showingProgressBar) {
            showingProgressBar = false;
            userCardList.remove(userCardList.size() - 1);
            adapter.notifyItemRemoved(userCardList.size());
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!fetching) {
                    refreshing = true;
                    offset = 0;
                    filter = query;
                    fetchUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!fetching) {
                    refreshing = true;
                    offset = 0;
                    filter = newText;
                    fetchUsers();
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ADD_USER) {
            if (resultCode == Activity.RESULT_OK) {
                refreshing = true;
                offset = 0;
                fetchUsers();
            }
        }
    }


    public abstract String getRole();
}
