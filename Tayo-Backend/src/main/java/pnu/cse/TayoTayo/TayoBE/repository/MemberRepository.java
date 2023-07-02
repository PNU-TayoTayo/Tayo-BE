package pnu.cse.TayoTayo.TayoBE.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    public void save(MemberEntity member){
        em.persist(member);
    }

    // 이메일 중복 체크 or 로그인 시
    public List<MemberEntity> findByEmail(String Email){
        return em.createQuery("select m from MemberEntity m where m.email =:email", MemberEntity.class)
                .setParameter("email",Email)
                .getResultList();
    }

}
