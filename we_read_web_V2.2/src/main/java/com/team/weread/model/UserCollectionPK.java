package com.team.weread.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;
@Data
public class UserCollectionPK implements Serializable {
    private Long userId;
    private Long bookId;
    private Byte collectionType;

    public UserCollectionPK() {}

    public UserCollectionPK(Long userId, Long bookId, Byte collectionType) {
        this.userId = userId;
        this.bookId = bookId;
        this.collectionType = collectionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCollectionPK that = (UserCollectionPK) o;
        return userId.equals(that.userId) &&
               bookId.equals(that.bookId) &&
               collectionType.equals(that.collectionType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, bookId, collectionType);
    }
}