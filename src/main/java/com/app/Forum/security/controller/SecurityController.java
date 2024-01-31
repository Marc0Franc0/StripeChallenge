package com.app.Forum.security.controller;

import com.app.Forum.security.dto.SignUpDTO;
import com.app.Forum.security.dto.SignInDTO;
import com.app.Forum.security.service.UserEntityServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/auth")
@RestController
@SecurityRequirement(name="Bearer Authentication")
public class SecurityController {
    @Autowired
    UserEntityServiceImpl userEntityService;

    //Endpoint para poder registrarse
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDTO authDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(userEntityService.registerUser(authDTO));
    }

    //Endpoint para iniciar sesión y obtener el token de autenticación
    //El mismo se complmenta con el filtro de autenticación
    @PostMapping("/login")
    public void authenticateUser(@RequestBody SignInDTO authDTO){
        userEntityService.authenticate(authDTO);
    }
}
