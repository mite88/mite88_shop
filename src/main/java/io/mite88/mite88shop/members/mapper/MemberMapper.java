package io.mite88.mite88shop.members.mapper;

import io.mite88.mite88shop.members.dto.MemberDescription;
import io.mite88.mite88shop.members.dto.MemberDetails;
import io.mite88.mite88shop.members.entity.Member;

public class MemberMapper {

    public static MemberDescription toDescription(Member member) {
        return new MemberDescription(
                member.getUsername(),
                member.getEmail(),
                member.getRole(),
                member.getCreatedAt()
        );
    }

    public static MemberDetails toDetails(Member member) {
        return new MemberDetails(
                member.getUsername(),
                member.getPassword(),
                member.getRole()
        );
    }

}
