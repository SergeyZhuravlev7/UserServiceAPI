package ru.aston.UserServiceAPI.hateoas;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import ru.aston.UserServiceAPI.controllers.UserControllerImpl;
import ru.aston.UserServiceAPI.dtos.UserDTOResponse;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UserAssembler implements RepresentationModelAssembler<UserDTOResponse, EntityModel<UserDTOResponse>> {


    @Override
    public EntityModel<UserDTOResponse> toModel(UserDTOResponse dto) {
        EntityModel<UserDTOResponse> entityModel = EntityModel.of(dto);
        entityModel.add(linkTo(methodOn(UserControllerImpl.class)
                .getUser(dto.getId(),null,null))
                .withSelfRel()
                .expand());
        entityModel.add(linkTo(methodOn(UserControllerImpl.class)
                .deleteUser(dto.getId()))
                .withRel("Delete this user")
                .withType("DELETE"));
        entityModel.add(linkTo(methodOn(UserControllerImpl.class)
                .updateUser(dto.getId(),null,null))
                .withRel("Update this user")
                .withType("PUT"));
        return entityModel;
    }

    @Override
    public CollectionModel<EntityModel<UserDTOResponse>> toCollectionModel(Iterable<? extends UserDTOResponse> entities) {
        return RepresentationModelAssembler.super
                .toCollectionModel(entities)
                .add(linkTo(methodOn(UserControllerImpl.class)
                        .getAllUsers(null,null,null))
                        .withSelfRel()
                        .withType("GET")
                        .andAffordance(afford(methodOn(UserControllerImpl.class)
                                .createUser(null,null))));
    }
}
