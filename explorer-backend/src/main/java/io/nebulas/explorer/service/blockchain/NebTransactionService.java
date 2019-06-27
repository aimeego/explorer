package io.nebulas.explorer.service.blockchain;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.nebulas.explorer.domain.*;
import io.nebulas.explorer.enums.NebTokenEnum;
import io.nebulas.explorer.mapper.NebContractTokenMapper;
import io.nebulas.explorer.mapper.NebPendingTransactionMapper;
import io.nebulas.explorer.mapper.NebTransactionMapper;
import io.nebulas.explorer.model.vo.Nrc20TransactionVo;
import io.nebulas.explorer.model.vo.TransactionVo;
import io.nebulas.explorer.util.DecodeUtil;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Nebulas transaction related operation service
 *
 * @author nathan wang
 * @version 1.0
 * @since 2018-01-25
 */
@AllArgsConstructor
@Service
public class NebTransactionService {
    private final NebTransactionMapper nebTransactionMapper;
    private final NebPendingTransactionMapper nebPendingTransactionMapper;
    private final NebContractTokenMapper nebContractTokenMapper;



    private static final Base64.Decoder DECODER = Base64.getDecoder();

    //数据库很慢，比RPC还慢，暂时弃用
    public boolean hasContractTransfer(String address, String contract) {
        if (StringUtils.isEmpty(address) || StringUtils.isEmpty(contract)) {
            return false;
        }
        return nebTransactionMapper.countTxnCntByFromAndTo(address, contract) > 0;
    }

    /**
     * save transaction information
     *
     * @param transaction transaction bean
     * @return saved result
     */
    public boolean addNebTransaction(NebTransaction transaction) {
        return nebTransactionMapper.addNebTransaction(transaction) > 0;
    }

    public Integer deleteNebTransactionByBlkHeight(long blkHeight) {
        return nebTransactionMapper.deleteByBlkHeight(blkHeight);
    }

    /**
     * save pending transaction information
     *
     * @param transaction pending transaction
     * @return saved result
     */
    public boolean addNebPendingTransaction(NebPendingTransaction transaction) {
        return nebPendingTransactionMapper.add(transaction) > 0;
    }

    /**
     * delete pending transaction
     *
     * @param hash pending transaction hash
     * @return deleted result
     */
    public boolean deleteNebPendingTransaction(String hash) {
        return nebPendingTransactionMapper.delete(hash) > 0;
    }

    /**
     * delete pending transaction
     *
     * @return deleted result
     */
    public boolean deleteNebPendingTransactionByTimestamp(Date ts) {
        return nebPendingTransactionMapper.deleteByTimestamp(ts) > 0;
    }

    /**
     * query transaction total count
     *
     * @param blockHeight block height
     * @param addressHash address hash
     * @return the number of transaction
     */
    public long countTxnCnt(Long blockHeight, String addressHash) {
        return nebTransactionMapper.countTxnCntByCondition(blockHeight, addressHash);
    }

    public long countContractTransfer(String contract) {
        return nebTransactionMapper.countContractTransfer(contract);
    }

    /**
     * according address hash to query transaction total count
     *
     * @param addressHash address hash
     * @return the number of transaction
     */
    public long countTxnCntByFromTo(String addressHash) {
        if (StringUtils.isEmpty(addressHash)) {
            return 0L;
        }
        return nebTransactionMapper.countTxnCntByFromTo(addressHash);
    }

    /**
     * according address hash to query pending transaction total count
     *
     * @param addressHash address hash
     * @return the number of transaction
     */
    public long countPendingTxnCnt(String addressHash) {
        return nebPendingTransactionMapper.countPendingTxnCntByCondition(addressHash);
    }

    public long countPendingContractTransaction(String contract) {
        if (StringUtils.isEmpty(contract)) {
            return 0L;
        }
        return nebPendingTransactionMapper.countPendingContractTransaction(contract);
    }

    public long countTxnCntByBlockHeight(Long blockHeight) {
        return nebTransactionMapper.countTxnCntByBlockHeight(blockHeight);
    }

    public long countTotalTxnCnt(){
        return nebTransactionMapper.countTotalTxnCnt();
    }

    /**
     * count address total nrc20 transfer transaction
     *
     * @param addressHash
     * @return the number of nrc20 transaction
     */
    public long countNrc20Txn(String addressHash) {
        if (StringUtils.isEmpty(addressHash)) {
            return 0L;
        }
        return nebTransactionMapper.countNrc20TxnCntByFromAndTo(addressHash);
    }

    /**
     * get address nrc20 transfer transaction
     *
     * @param addressHash:the nas address
     * @return the list of nrc20 transaction in page
     */
    public List<Nrc20TransactionVo> getNrc20Transactions(String addressHash) {

        //只允许官方支持的nrc20代币可以用来展示,所以查询出来的交易也只有nrc20的
        List<NebContractToken> contractToken = nebContractTokenMapper.getAllContractTokens();
        Map<String, Long> contractMap = contractToken.stream().collect(Collectors.toMap(NebContractToken::getContract,NebContractToken::getTokenDecimals));

        //搜索所有nrc20转账记录，然后提取属于自己地址的
        List<NebTransaction> contractTxList = new ArrayList<>();
        contractToken.forEach(nebContractToken -> {

            List<NebTransaction> contractList = nebTransactionMapper.findTxnByContract(nebContractToken.getContract());
            if (contractList == null || contractList.size() == 0){
                contractList = Collections.emptyList();
            }
            contractTxList.addAll(nebTransactionMapper.findTxnByContract(nebContractToken.getContract()));
        });

        List<Nrc20TransactionVo> nrc20TxList = Lists.newLinkedList();
        //过滤掉非nrc20的数据,并且提取对应的data,提取属于自己地址的交易
        contractTxList.forEach(nebTransaction -> {
            JSONObject data = DecodeUtil.decodeData(nebTransaction.getData());
            if (DecodeUtil.isContractTransfer(data)){
                //将to的合约地址放到对应的contractAddress字段里
                nebTransaction.setContractAddress(nebTransaction.getTo());
                JSONArray args = data.getJSONArray("Args");
                //"Args" -> "["n1JdmmyhrrqBuESseZSbrBucnvugSewSMTE","9299123456789987654321"]"
                String to = args.get(0).toString();
                String value = args.get(1).toString();
                nebTransaction.setTo(to);
                nebTransaction.setValue(value);

                if(nebTransaction.getFrom().equals(addressHash) || nebTransaction.getTo().equals(addressHash)){
                    Nrc20TransactionVo nrc20TransactionVo = new Nrc20TransactionVo();
                    nrc20TransactionVo.buildFromNebTransaction(nebTransaction);
                    nrc20TransactionVo.setTokenDecimals(contractMap.get(nrc20TransactionVo.getContractAddress()));
                    nrc20TxList.add(nrc20TransactionVo);
                }
            }
        });

        Collections.sort(nrc20TxList,Comparator.comparing(Nrc20TransactionVo::getTimestamp).reversed());

        return nrc20TxList;
    }


    /**
     * According to transaction hash query transaction information
     *
     * @param hash transaction hash
     * @return transaction information
     */
    public NebTransaction getNebTransactionByHash(String hash) {
        if (StringUtils.isEmpty(hash)) {
            return null;
        }
        return nebTransactionMapper.getByHash(hash);
    }

    /**
     * According to contract address query transaction information
     * ps: return null when contract address is empty
     *
     * @param contractAddress contractAddress hash
     * @return transaction information
     */
    public NebTransaction getNebTransactionByContractAddress(String contractAddress) {
        if (StringUtils.isEmpty(contractAddress)) {
            return null;
        }
        return nebTransactionMapper.getByContractAddress(contractAddress);
    }

    /**
     * According to pending transaction hash query pending transaction information
     *
     * @param hash pending transaction hash
     * @return pending transaction information
     */
    public NebPendingTransaction getNebPendingTransactionByHash(String hash) {
        if (StringUtils.isEmpty(hash)) {
            return null;
        }
        return nebPendingTransactionMapper.getByHash(hash);
    }

    public BlockSummary getBlockSummaryByBlockHeight(long blockHeight) {
        return covertBlockSummary(blockHeight, nebTransactionMapper.findTxnByBlockHeight(blockHeight));
    }

    /**
     * According to block height query transaction information
     *
     * @param blockHeight block height
     * @return transaction list
     */
    public List<NebTransaction> findTxnByBlockHeight(Long blockHeight) {
        return nebTransactionMapper.findTxnByBlockHeight(blockHeight);
    }

    /**
     * According to condition query transaction information
     *
     * @param blockHeight block height
     * @param addressHash address hash
     * @param page        current page
     * @param pageSize    number of information per page
     * @return transaction list
     */
    public List<NebTransaction> findTxnByCondition(Long blockHeight, String addressHash, int page, int pageSize) {
        return nebTransactionMapper.findTxnByCondition(blockHeight, addressHash, (page - 1) * pageSize, pageSize);
    }

    /**
     * According to condition query pending transaction information
     *
     * @param addressHash address hash
     * @param page        current page
     * @param pageSize    number of information per page
     * @return pending transaction list
     */
    public List<NebPendingTransaction> findPendingTxnByCondition(String addressHash, int page, int pageSize) {
        return nebPendingTransactionMapper.findPendingTxnByCondition(addressHash, (page - 1) * pageSize, pageSize);
    }

    public List<NebPendingTransaction> findLessThanTimestamp(Date timestamp, int limit) {
        return nebPendingTransactionMapper.findLessThanTimestamp(timestamp, limit);
    }

    public List<NebPendingTransaction> findPendingContractTransactions(String contract, int page, int pageSize) {
        if (StringUtils.isEmpty(contract)) {
            return Collections.emptyList();
        }
        return nebPendingTransactionMapper.findPendingContractTransactions(contract, (page - 1) * pageSize, pageSize);
    }

    public List<NebTransaction> findContractTransactions(String contract, int page, int pageSize) {
        if (StringUtils.isEmpty(contract)) {
            return Collections.emptyList();
        }
        return nebTransactionMapper.findContractTransactions(contract, (page - 1) * pageSize, pageSize);
    }

    /**
     * query transaction information list
     *
     * @param addressHash address hash
     * @param page        current page
     * @param pageSize    number of information per page
     * @return transaction list
     */
    public List<NebTransaction> findTxnByFromTo(String addressHash, int page, int pageSize) {
        if (StringUtils.isEmpty(addressHash)) {
            return Collections.emptyList();
        }
        return nebTransactionMapper.findTxnByFromTo(addressHash, (page - 1) * pageSize, pageSize);
    }

    /**
     * query transaction information list
     *
     * @param page     current page
     * @param pageSize number of information per page
     * @return transaction list
     */
    public List<NebTransaction> findTxnOrderById(int page, int pageSize) {
        return nebTransactionMapper.findTxnOrderById((page - 1) * pageSize, pageSize);
    }

    /**
     * calculate transaction count between date
     *
     * @param from begin date
     * @param to   end date
     * @return transaction count map
     */
    public Map<String, Long> countTxCntGroupMapByTimestamp(Date from, Date to) {
        List<Map<String, Object>> txCntResultList = nebTransactionMapper.countTxCntGroupByTimestamp(parseDate2Str(from), parseDate2Str(to));
        Map<String, Long> txCntMap = txCntResultList.stream()
                .collect(Collectors.toMap(k -> k.get("ts").toString(), v -> Long.valueOf(v.get("cnt").toString())));

        Map<String, Long> resultMap = Maps.newLinkedHashMap();
        LocalDate fromLocalDate = LocalDate.fromDateFields(from);
        LocalDate toLocalDate = LocalDate.fromDateFields(to);
        while (fromLocalDate.isBefore(toLocalDate)) {
            String dateStr = fromLocalDate.toString("yyyy-MM-dd");
            Long cnt = txCntMap.get(dateStr);
            resultMap.put(dateStr, (null != cnt ? cnt : 0));
            fromLocalDate = fromLocalDate.plusDays(1);
        }
        return resultMap;
    }

    public long countTxToday(){

        List<Map<String, Object>> txCntResultList = nebTransactionMapper.countTxCntGroupByTimestamp(LocalDate.now(DateTimeZone.UTC).toDateTimeAtStartOfDay().toString(), LocalDateTime.now(DateTimeZone.UTC).toString());
        if (txCntResultList.size() == 0){
            return 0;
        }
        long todayTxCount = (long)txCntResultList.get(0).get("cnt");
        return todayTxCount;
    }




    /**
     * According to block height calculate transaction information
     *
     * @param blockHeights block height list
     * @return summary map
     */
    public Map<Long, BlockSummary> calculateTxnSummaryInBlock(List<Long> blockHeights, boolean isWithGas) {
        if (CollectionUtils.isEmpty(blockHeights)) {
            return Collections.emptyMap();
        }

        List<NebTransaction> txnList = nebTransactionMapper.findByBlockHeights(blockHeights);
        Map<Long, List<NebTransaction>> txnMap = txnList.stream().collect(Collectors.groupingBy(NebTransaction::getBlockHeight));

        Map<Long, BlockSummary> summaryMap = Maps.newHashMap();
        if (isWithGas) {
            blockHeights.forEach(height -> summaryMap.put(height, covertBlockSummary(height, txnMap.get(height))));
        } else {
            txnMap.forEach((k, v) -> summaryMap.put(k, new BlockSummary(k, v.size())));
            blockHeights.forEach(height -> {
                List<NebTransaction> txList = txnMap.get(height);
                summaryMap.put(height, new BlockSummary(height, CollectionUtils.isNotEmpty(txList) ? txList.size() : 0L));
            });
        }
        return summaryMap;
    }

    private BlockSummary covertBlockSummary(long blockHeight, List<NebTransaction> txs) {
        if (CollectionUtils.isEmpty(txs)) {
            return new BlockSummary(blockHeight, 0, null, 0L, null);
        } else {
            BigDecimal blkGasRewardBd = BigDecimal.ZERO;
            BigDecimal blkTotalGasPriceBd = BigDecimal.ZERO;
            BigDecimal blkTotalGasLimitBd = BigDecimal.ZERO;

            for (NebTransaction tx : txs) {
                blkGasRewardBd = blkGasRewardBd.add(tx.getGasUsedBd().multiply(tx.getGasPriceBd()));
                blkTotalGasPriceBd = blkTotalGasPriceBd.add(tx.getGasPriceBd());
                blkTotalGasLimitBd = blkTotalGasLimitBd.add(tx.getGasLimitBd());
            }

            String gasReward = blkGasRewardBd.toPlainString();
            long gasLimit = blkTotalGasLimitBd.longValue();
            String avgGasPrice = blkTotalGasPriceBd.divide(new BigDecimal(txs.size()), 2, BigDecimal.ROUND_DOWN).toPlainString();
            return new BlockSummary(blockHeight, txs.size(), gasReward, gasLimit, avgGasPrice);
        }
    }

    /**
     * According to address hash calculate transaction count
     *
     * @param addressHashes address hash
     * @return transaction count map
     */
    public Map<String, Long> countTxnCntByFromTo(List<String> addressHashes) {
        if (CollectionUtils.isEmpty(addressHashes)) {
            return Collections.emptyMap();
        }
        List<Map<String, String>> fromList = nebTransactionMapper.countTxnCntMapByFrom(addressHashes);
        Map<String, Long> fromCntMap = fromList.stream()
                .collect(Collectors.toMap(k -> k.get("from"), v -> Long.parseLong(String.valueOf(v.get("cnt")))));

        List<Map<String, String>> toList = nebTransactionMapper.countTxnCntMapByTo(addressHashes);
        Map<String, Long> toCntMap = toList.stream()
                .collect(Collectors.toMap(k -> k.get("to"), v -> Long.parseLong(String.valueOf(v.get("cnt")))));

        return addressHashes.stream().collect(Collectors.toMap(k -> k, v -> {
            Long fromCnt = fromCntMap.get(v);
            Long toCnt = toCntMap.get(v);
            return (fromCnt == null ? 0 : fromCnt) + (toCnt == null ? 0 : toCnt);
        }));

    }

    private String parseDate2Str(Date date) {
        return LocalDate.fromDateFields(date).toString("yyyy-MM-dd");
    }

}
