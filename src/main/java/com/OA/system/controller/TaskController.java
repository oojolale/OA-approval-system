package com.OA.system.controller;

import com.OA.system.dto.ApiResponse;
import com.OA.system.dto.CompleteTaskRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/OA/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    /*查询经理待办*/
    @GetMapping("/todo")
    public ApiResponse<List<TaskItem>> todo(@RequestParam String assignee) {
        List<TaskItem> items = taskService.createTaskQuery()
                .taskAssignee(assignee)
                .orderByTaskCreateTime().desc()
                .list()
                .stream().map(TaskItem::of).collect(Collectors.toList());
        return ApiResponse.ok(items);
    }

    /*经理审批完成*/
    @PostMapping("/complete")
    public ApiResponse<String> complete(@RequestBody CompleteTaskRequest req) {
        // 简单演示：校验并添加意见
        Task task = taskService.createTaskQuery().taskId(req.getTaskId()).singleResult();
        if (task == null) return ApiResponse.fail("task not found");
        if (!req.getApprover().equals(task.getAssignee()))
            return ApiResponse.fail("approver mismatch with assignee");

        if (req.getComment() != null && !req.getComment().isEmpty()) {
            taskService.addComment(req.getTaskId(), task.getProcessInstanceId(), req.getComment());
        }
        taskService.complete(req.getTaskId());
        return ApiResponse.ok("completed");
    }

    @Data
    static class TaskItem {
        private String taskId;
        private String name;
        private String procInsId;
        private String assignee;

        static TaskItem of(Task t) {
            TaskItem i = new TaskItem();
            i.setTaskId(t.getId());
            i.setName(t.getName());
            i.setProcInsId(t.getProcessInstanceId());
            i.setAssignee(t.getAssignee());
            return i;
        }
    }
}
