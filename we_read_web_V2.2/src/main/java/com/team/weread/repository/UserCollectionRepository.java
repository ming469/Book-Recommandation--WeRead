package com.team.weread.repository;

import com.team.weread.model.UserCollection;
import com.team.weread.model.UserCollectionPK;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 用户收藏数据访问接口
 * <p>
 * 提供用户收藏相关的数据库操作方法
 * </p>
 *
 * @author WeRead Team
 * @since 1.0.0
 */
public interface UserCollectionRepository extends JpaRepository<UserCollection, UserCollectionPK> {
    
    /**
     * 根据用户ID、图书ID和收藏类型查找收藏记录
     *
     * @param userId 用户ID
     * @param bookId 图书ID
     * @param collectionType 收藏类型
     * @return 符合条件的收藏记录，如果不存在则返回null
     */
    UserCollection findByUserIdAndBookIdAndCollectionType(Long userId, Long bookId, Byte collectionType);
    
    /**
     * 根据用户ID查找所有收藏记录
     *
     * @param userId 用户ID
     * @return 该用户的所有收藏记录列表
     */
    List<UserCollection> findAllByUserId(Long userId);
    
    /**
     * 根据用户ID和收藏类型查找收藏记录
     *
     * @param userId 用户ID
     * @param collectionType 收藏类型
     * @return 符合条件的收藏记录列表
     */
    List<UserCollection> findByUserIdAndCollectionType(Long userId, Byte collectionType);
    
    /**
     * 根据图书ID查找所有收藏该图书的记录
     *
     * @param bookId 图书ID
     * @return 收藏该图书的所有记录列表
     */
    List<UserCollection> findAllByBookId(Long bookId);
    
    /**
     * 删除指定用户对指定图书的特定类型收藏
     *
     * @param userId 用户ID
     * @param bookId 图书ID
     * @param collectionType 收藏类型
     */
    void deleteByUserIdAndBookIdAndCollectionType(Long userId, Long bookId, Byte collectionType);
}