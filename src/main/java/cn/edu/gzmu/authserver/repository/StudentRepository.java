package cn.edu.gzmu.authserver.repository;


import cn.edu.gzmu.authserver.base.BaseRepository;
import cn.edu.gzmu.authserver.model.constant.AuthConstant;
import cn.edu.gzmu.authserver.model.entity.Student;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

/**
 * Student Repository
 *
 * @author echo
 * @version 1.0
 * @date 2019-5-23 17:38:13
 */
@RepositoryRestResource(path = AuthConstant.STUDENT)
public interface StudentRepository extends BaseRepository<Student, Long> {

    /**
     * 通过用户 id 查询学生
     *
     * @param userId 用户 id
     * @return 结果
     */
    @RestResource(path = "byUserId")
    Optional<Student> findFirstByUserId(Long userId);

    /**
     * 查询在指定 user ids 的学生信息
     *
     * @param userIds 用户 ids
     * @return 学生信息
     */
    @RestResource(path = "byUserIds")
    List<Student> findAllByUserIdIn(List<Long> userIds);

    /**
     * 通过班级id查询学生
     *
     * @param classId 班级id
     * @return 学生
     */
    List<Student> findAllByClassesId(Long classId);
}