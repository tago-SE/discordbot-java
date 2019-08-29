package db.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Users {

    private HashMap<String, BNetUser> users = new HashMap<>();  // HashMap of all users

    public void insert(BNetUser user) {
        if (user == null || user.name == null)
            throw new NullPointerException();
        String key = user.name.toLowerCase();
        if (users.containsKey(key))
            throw new IllegalStateException("Already contains key=" + key);
        users.put(key, user);
    }

    public BNetUser delete(BNetUser user) {
        if (user == null || user.name == null)
            throw new NullPointerException();
        return users.remove(user.name.toLowerCase());
    }

    public BNetUser findOneByName(String name) {
        return users.get(name.toLowerCase());
    }

    public List<BNetUser> findManyByName(String name) {
        List<BNetUser> matchingUsers = new ArrayList<>();
        name = name.toLowerCase();
        for (BNetUser u : users.values()) {
            if (u.name.toLowerCase().contains(name)) {
                matchingUsers.add(u);
            }
        }
        return matchingUsers;
    }

    public boolean exists(String name) {
        return findOneByName(name) != null;
    }

    public int count() {
        return users.size();
    }

    @Override
    public String toString() {
        return "Users{" +
                "users=" + users +
                '}';
    }
}
