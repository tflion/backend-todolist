package br.com.thalesleao.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.thalesleao.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class filterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        var servletPath = request.getServletPath();
        if (servletPath.startsWith("/tasks/")) {
            var auth = request.getHeader("Authorization").substring("Basic".length()).trim();
            String base64AuthDecoded = new String(Base64.getDecoder().decode(auth));
            String[] credentials = base64AuthDecoded.split(":");
            String username = credentials[0];
            String password = credentials[1];

            var user = userRepository.findByUsername(username);
            if (user == null) {
                response.sendError(401, "Usuário não cadastrado.");
            } else {
                var isPasswordCorrect = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword()).verified;
                if (!isPasswordCorrect) {
                    response.sendError(401, "Nenhum usuário encontrado com os dados informados.");
                } else {
                    request.setAttribute("userId", user.getId());
                    chain.doFilter(request, response);
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}