package io.mite88.mite88shop.members.mapper;

import io.mite88.mite88shop.members.dto.MemberDescription;
import io.mite88.mite88shop.members.dto.MemberDetails;
import io.mite88.mite88shop.members.entity.Member;

public class MemberMapper {

    /**
     * Member 엔티티 → MemberDescription 변환 (API 응답용)
     */
    public static MemberDescription toDescription(Member member) {
        return new MemberDescription(
                member.getUsername(),
                member.getEmail(),
                member.getRole(),
                member.getCreatedAt()
        );
    }

    /**
     * Member 엔티티 → MemberDetails 변환 (Spring Security 인증용)
     */
    public static MemberDetails toDetails(Member member) {
        return new MemberDetails(
                member.getUsername(),
                member.getPassword(),
                member.getRole()
        );
    }

}
