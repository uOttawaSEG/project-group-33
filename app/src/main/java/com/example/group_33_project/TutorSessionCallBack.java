package com.example.group_33_project;

import java.util.List;
import java.util.Map;

public interface TutorSessionCallBack {
    void onSuccess(List<Map<String, Object>> slots);
    void onFailure(String msg);
}
