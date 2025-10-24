package ru.aston.UserServiceAPI.controllers;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import ru.aston.UserServiceAPI.Utils.GlobalExceptionHandler;
import ru.aston.UserServiceAPI.dtos.UserDTORequest;
import ru.aston.UserServiceAPI.dtos.UserDTOResponse;

@OpenAPIDefinition (info = @Info (title = "UserServiceAPI", description = "Маленькая апишечка для большого дела)"))
@ApiResponses ({@ApiResponse (responseCode = "500", description = "Неизвестная ошибка.",
        content = @Content (
                mediaType = "application/json",
                schema = @Schema (implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse (responseCode = "404", description = "Ресурс не найден.",
                content = @Content (
                        mediaType = "application/json",
                        schema = @Schema (implementation = GlobalExceptionHandler.ErrorResponse.class)))})
public interface UserController {

    @ApiResponses (value = {
            @ApiResponse (responseCode = "200", description = "Пользователь успешно найден",
                    content = @Content (schema = @Schema (implementation = EntityModel.class),
                            examples = @ExampleObject (value = """
                                    {
                                        "id": 1,
                                        "name": "Somename",
                                        "email": "someemail2@gmail.com",
                                        "age": 31,
                                        "_links": {
                                            "self": {
                                                "href": "http://localhost:8080/user?id=1"
                                            },
                                            "Delete this user": {
                                                "href": "http://localhost:8080/user?id=1",
                                                "type": "DELETE"
                                            },
                                            "Update this user": {
                                                "href": "http://localhost:8080/user?id=1",
                                                "type": "PUT"
                                            }
                                        },
                                        "_templates": {
                                            "default": {
                                                "method": "DELETE",
                                                "properties": []
                                            },
                                            "updateUser": {
                                                "method": "PUT",
                                                "properties": [
                                                    {
                                                        "name": "age",
                                                        "required": true,
                                                        "min": 18,
                                                        "max": 99,
                                                        "type": "number"
                                                    },
                                                    {
                                                        "name": "email",
                                                        "required": true,
                                                        "type": "email"
                                                    },
                                                    {
                                                        "name": "name",
                                                        "regex": "^[A-Z][a-z]{2,14}$",
                                                        "required": true,
                                                        "type": "text"
                                                    }
                                                ]
                                            }
                                        }
                                    }"""))),
            @ApiResponse (responseCode = "404", description = "Пользователь с такими параметрами не найден.",
                    content = @Content (
                            mediaType = "application/json",
                            schema = @Schema (implementation = GlobalExceptionHandler.ErrorResponse.class)
                    ))}
    )
    @Operation (summary = "Поиск существующего пользователя по айди, имени или адресу электронной почты." +
            "Параметры являются опциональными, при наличии айди поиск будет осуществлен по айди.",
            description = "Возвращает найденного пользователя и ссылки на допустимые действия.")
    @Tag (name = "Гет методы")
    ResponseEntity<EntityModel<UserDTOResponse>> getUser(
            @Parameter (description = "Айди, по которому осуществляется поиск.")
            Long id,
            @Parameter (description = "Имя, по которому осуществляется поиск.")
            String name,
            @Parameter (description = "Адрес электронной почты, по которому осуществляется поиск.")
            String email);

    @ApiResponses (value = {
            @ApiResponse (responseCode = "200", description = "Пользователи успешно найдены",
                    content = @Content (schema = @Schema (implementation = CollectionModel.class),
                            examples = @ExampleObject (value = """
                                    {
                                        "_embedded": {
                                            "userDTOResponseList": [
                                                {
                                                    "id": 1,
                                                    "name": "Somename",
                                                    "email": "someemail2@gmail.com",
                                                    "age": 31,
                                                    "_links": {
                                                        "self": {
                                                            "href": "http://localhost:8080/user?id=1"
                                                        },
                                                        "Delete this user": {
                                                            "href": "http://localhost:8080/user?id=1",
                                                            "type": "DELETE"
                                                        },
                                                        "Update this user": {
                                                            "href": "http://localhost:8080/user?id=1",
                                                            "type": "PUT"
                                                        }
                                                    },
                                                    "_templates": {
                                                        "default": {
                                                            "method": "DELETE",
                                                            "properties": []
                                                        },
                                                        "updateUser": {
                                                            "method": "PUT",
                                                            "properties": [
                                                                {
                                                                    "name": "age",
                                                                    "required": true,
                                                                    "min": 18,
                                                                    "max": 99,
                                                                    "type": "number"
                                                                },
                                                                {
                                                                    "name": "email",
                                                                    "required": true,
                                                                    "type": "email"
                                                                },
                                                                {
                                                                    "name": "name",
                                                                    "regex": "^[A-Z][a-z]{2,14}$",
                                                                    "required": true,
                                                                    "type": "text"
                                                                }
                                                            ]
                                                        }
                                                    }
                                                }
                                            ]
                                        },
                                        "_links": {
                                            "self": {
                                                "href": "http://localhost:8080/user/all{?page,size,sort}",
                                                "type": "GET",
                                                "templated": true
                                            }
                                        },
                                        "_templates": {
                                            "default": {
                                                "method": "POST",
                                                "properties": [
                                                    {
                                                        "name": "age",
                                                        "required": true,
                                                        "min": 18,
                                                        "max": 99,
                                                        "type": "number"
                                                    },
                                                    {
                                                        "name": "email",
                                                        "required": true,
                                                        "type": "email"
                                                    },
                                                    {
                                                        "name": "name",
                                                        "regex": "^[A-Z][a-z]{2,14}$",
                                                        "required": true,
                                                        "type": "text"
                                                    }
                                                ],
                                                "target": "http://localhost:8080/user"
                                            }
                                        }
                                    }""")))
    })
    @Operation (summary = "Получение списка существующих пользователей. Доступна пагинация и сортировка, параметры опциональны.",
            description = "Возвращает список найденных пользователей в соответствии с переданными параметрами " +
                    "и ссылки на доступные действия.")
    @Tag (name = "Гет методы")
    ResponseEntity<CollectionModel<EntityModel<UserDTOResponse>>> getAllUsers(
            @Parameter (description = "Номер страницы, используется вместе с размером страницы.")
            Integer page,
            @Parameter (description = "Размер страницы, используется вместе с номером страницы.")
            Integer size,
            @Parameter (description = "Сортировка списка по возрастанию либо по убыванию(asc или desc).")
            String sort);

    @ApiResponses (value = {
            @ApiResponse (responseCode = "200", description = "Пользователь успешно создан",
                    content = @Content (
                            schema = @Schema (implementation = UserDTOResponse.class),
                            examples = {@ExampleObject (value = """
                                    {
                                        "id": 1,
                                        "name": "Somename",
                                        "email": "someemail2@gmail.com",
                                        "age": "31"
                                    }
                                    """)})),
            @ApiResponse (responseCode = "400", description = "Запрос не соответствует структуре API, либо пользователь не прошел валидацию.",
                    content = @Content (
                            mediaType = "application/json",
                            examples = {@ExampleObject (
                                    name = "Пользователь не валидный",
                                    value = """
                                            {
                                                "errors": {
                                                    "name": "Name should start with upper case letter and should be between 3 and 15 letters.",
                                                    "email": "Email should be a valid email.",
                                                    "age": "Age should be equals or greater 18 years old."
                                                },
                                                "timestamp": "2025-10-24T15:19:59.1703931"
                                            }"""
                            ),
                                    @ExampleObject (
                                            name = "Запрос не соответствует структуре API.",
                                            value = """
                                                    {
                                                         "error": "Request body should not be empty, and should contains name, email, age.",
                                                         "timestamp": "2025-10-24T15:23:54.2398034"
                                                    }"""
                                    )}
                    )
            )

    })
    @Operation (summary = "Создание нового пользователя", description = "Возвращает созданного пользователя.")
    ResponseEntity<UserDTOResponse> createUser(
            @Parameter (required = true, description = "Новый пользователь в формате JSON.")
            UserDTORequest userDTORequest,
            BindingResult bindingResult);

    @ApiResponses (value = {
            @ApiResponse (responseCode = "200", description = "Пользователь успешно удален.",
                    content = @Content (
                            schema = @Schema (implementation = UserDTOResponse.class),
                            examples = {@ExampleObject (value = """
                                    {
                                        "id": 1,
                                        "name": "Somename",
                                        "email": "someemail2@gmail.com",
                                        "age": "31"
                                    }
                                    """)})),
            @ApiResponse (responseCode = "404", description = "Пользователь с таким айди не существует.",
                    content = @Content (
                            mediaType = "application/json",
                            schema = @Schema (implementation = GlobalExceptionHandler.ErrorResponse.class)))})
    @Operation (summary = "Удаление пользователя",
            description = "Возвращает удаленного пользователя.")
    ResponseEntity<UserDTOResponse> deleteUser(
            @Parameter (required = true, description = "Айди пользователя, которого необходимо удалить.")
            Long id);

    @ApiResponses (value = {
            @ApiResponse (responseCode = "200", description = "Пользователь успешно обновлен.",
                    content = @Content (
                            schema = @Schema (implementation = UserDTOResponse.class),
                            examples = {@ExampleObject (value = """
                                    {
                                        "id": 1,
                                        "name": "Somename",
                                        "email": "someemail2@gmail.com",
                                        "age": "31"
                                    }
                                    """)})),
            @ApiResponse (responseCode = "400", description = "Запрос не соответствует структуре API, либо пользователь не прошел валидацию.",
                    content = @Content (
                            mediaType = "application/json",
                            examples = {@ExampleObject (
                                    name = "Пользователь не валидный",
                                    value = """
                                            {
                                                "errors": {
                                                    "name": "Name should start with upper case letter and should be between 3 and 15 letters.",
                                                    "email": "Email should be a valid email.",
                                                    "age": "Age should be equals or greater 18 years old."
                                                },
                                                "timestamp": "2025-10-24T15:19:59.1703931"
                                            }"""
                            ),
                                    @ExampleObject (
                                            name = "Запрос не соответствует структуре API.",
                                            value = """
                                                    {
                                                         "error": "Request body should not be empty, and should contains name, email, age.",
                                                         "timestamp": "2025-10-24T15:23:54.2398034"
                                                    }"""
                                    )}
                    )
            ),
            @ApiResponse (responseCode = "404", description = "Пользователь с таким айди не существует.",
                    content = @Content (
                            schema = @Schema (implementation = GlobalExceptionHandler.ErrorResponse.class)))

    })
    @Operation (summary = "Обновление пользователя",
            description = "Возвращает обновленного пользователя.")
    ResponseEntity<UserDTOResponse> updateUser(
            @Parameter (required = true,
                    description = "Айди обновляемого пользователя.")
            Long id,
            @Parameter (required = true,
                    description = "Обновленный пользователь в формате JSON.",
                    schema = @Schema (implementation = UserDTORequest.class))
            UserDTORequest userDTORequest,
            BindingResult bindingResult);
}
