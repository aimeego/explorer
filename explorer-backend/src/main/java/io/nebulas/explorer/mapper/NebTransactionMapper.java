package io.nebulas.explorer.mapper;

import io.nebulas.explorer.domain.BlockSummary;
import io.nebulas.explorer.domain.NebTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * the mapper of table neb_transaction
 *
 * @author nathan wang
 * @version 1.0
 * @since 2018-01-24
 */
@Mapper
public interface NebTransactionMapper {

    Integer addNebTransaction(NebTransaction transaction);

    Integer deleteByBlkHeight(@Param("blockHeight") long blockHeight);

    long countTxnCntByCondition(@Param("blockHeight") Long blockHeight, @Param("addressHash") String addressHash);

    long countTxnCntByFromTo(String addressHash);

    long countContractTransfer(String contract);

    long countTxnCntByFromAndTo(@Param("from") String from, @Param("to") String to);

    long countTxnCntByBlockHeight(Long blockHeight);

    long countTotalTxnCnt();

    long countNrc20TxnCntByFromAndTo(String addressHash);

    NebTransaction getByHash(String hash);

    NebTransaction getByContractAddress(String contractAddress);

    List<NebTransaction> findTxnByBlockHeight(Long blockHeight);

    List<NebTransaction> findTxnByCondition(@Param("blockHeight") Long blockHeight, @Param("addressHash") String addressHash, @Param("offset") int offset, @Param("limit") int limit);

    List<NebTransaction> findContractTransactions(@Param("contract") String contract, @Param("offset") int offset, @Param("limit") int limit);

    List<NebTransaction> findTxnByFromTo(@Param("addressHash") String addressHash, @Param("offset") int offset, @Param("limit") int limit);

    List<NebTransaction> findTxnOrderById(@Param("offset") int offset, @Param("limit") int limit);

    List<NebTransaction> findByBlockHeights(@Param("blockHeights") List<Long> blockHeights);

    List<NebTransaction> findTxnByFromToAndCall(@Param("addressHash") String addressHash,@Param("contractAddress") String contractAddress);

    List<NebTransaction> findTxnByContract(@Param("contractAddress") String contractAddress);

    List<BlockSummary> countTxnInBlock(@Param("blockHeights") List<Long> blockHeights);

    List<Map<String, String>> countTxnCntMapByFrom(List<String> addressHashes);

    List<Map<String, String>> countTxnCntMapByTo(List<String> addressHashes);

    List<Map<String, Object>> countTxCntGroupByTimestamp(@Param("from") String from, @Param("to") String to);
}
