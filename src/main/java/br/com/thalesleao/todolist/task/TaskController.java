package br.com.thalesleao.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.thalesleao.todolist.utils.utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/create")
    public ResponseEntity<TaskModel> create(@RequestBody TaskModel task, HttpServletRequest request) {
        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(task.getStartAt()) || currentDate.isAfter(task.getEndAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data de início/término não pode ser anterior a data atual.");
        }
        ;

        if (task.getStartAt().isAfter(task.getEndAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data de início não pode ser posterior a data de término.");
        }
        ;

        var userId = request.getAttribute("userId");
        task.setUserId((UUID) userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.taskRepository.save(task));
    }

    @GetMapping("/")
    public ResponseEntity<List<TaskModel>> getByUserId(HttpServletRequest request) {
        var userId = request.getAttribute("userId");
        return ResponseEntity.status(HttpStatus.OK).body(this.taskRepository.findByUserId((UUID) userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskModel> update(@RequestBody TaskModel task, HttpServletRequest request,
            @PathVariable UUID id) {
        var taskToUpdate = this.taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "A tarefa informada não foi encontrada."));

        var userId = request.getAttribute("userId");

        if (!taskToUpdate.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Usuário sem permissão de alterar esta tarefa.");
        }

        utils.copyNonNullProperties(task, taskToUpdate);

        return ResponseEntity.status(HttpStatus.OK).body(this.taskRepository.save(taskToUpdate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id, HttpServletRequest request) {
        var taskToDelete = this.taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "A tarefa informada não foi encontrada."));

        var userId = request.getAttribute("userId");

        if (!taskToDelete.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Usuário sem permissão de alterar esta tarefa.");
        }

        this.taskRepository.deleteById(taskToDelete.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .body("A tarefa " + taskToDelete.getTitle() + " foi excluída com sucesso.");
    }
}