package io.nebulas.explorer.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

import io.nebulas.explorer.domain.TokenRecord;

@Mapper
@Component
public interface TokenRecordMapper {

    @Insert("insert into token_record(token_name, address, txHash, block, amount, source, timestamp, createdAt) values(#{tokenName}, #{address}, #{txHash}, #{block}, #{amount}, #{source}, #{timestamp}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    public int insert(TokenRecord record);

    @Select("select * from token_record where address=#{address} order by `timestamp` desc limit #{offset}, #{limit}")
    List<TokenRecord> getByAddress(@Param("address") String address, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select * from token_record where address=#{address} and token_name=#{tokenName} order by `timestamp` desc limit #{offset}, #{limit}")
    List<TokenRecord> getByAddressAndTokenName(@Param("address") String address, @Param("tokenName") String tokenName, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from token_record where address=#{address}")
    long countByAddress(@Param("address") String address);
}

