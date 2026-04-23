package proxy.src.Sources.Users.Impl;

import proxy.src.Helpers.AppConfig;
import proxy.src.Models.User;
import proxy.src.Sources.Users.Interfaces.Users;

import java.util.List;

public class UsersFromFile implements Users {
    @Override
    public List<User> getListUsers() {
        return AppConfig.LIST_OF_USERS;
    }
}
