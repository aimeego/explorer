package io.nebulas.explorer.service.blockchain;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Comparator;

import io.nebulas.explorer.model.vo.Nrc20TransactionVo;
import io.nebulas.explorer.domain.TokenRecord;
import io.nebulas.explorer.domain.NebContractToken;
import io.nebulas.explorer.domain.NebTransaction;
import io.nebulas.explorer.mapper.TokenRecordMapper;
import io.nebulas.explorer.mapper.NebContractTokenMapper;
import io.nebulas.explorer.mapper.NebTransactionMapper;
import io.nebulas.explorer.util.DecodeUtil;
import lombok.AllArgsConstructor;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@AllArgsConstructor
@Service
public class TokenRecordService {

    private final TokenRecordMapper tokenRecord;
    private final NebTransactionMapper nebTransactionMapper;
    private final NebContractTokenMapper nebContractTokenMapper;

    /**
     * get address nrc20 transfer transaction from specific contract
     *
     * @param addressHash:the nas address
     * @param contractHash:the contract address
     * @return the list of nrc20 transaction in page
     */
    public List<Nrc20TransactionVo> getTokenTransactions(String addressHash, String contractHash) {

        //只允许官方支持的nrc20代币可以用来展示,所以查询出来的交易也只有官方支持的nrc20的
        NebContractToken contractToken = nebContractTokenMapper.getByContract(contractHash);
        List<NebTransaction> contractTxList = nebTransactionMapper.findTxnByContract(contractHash);
        if (contractTxList == null || contractTxList.size() == 0) {
            contractTxList = Collections.emptyList();
        }
        List<Nrc20TransactionVo> nrc20TxList = Lists.newLinkedList();
        contractTxList.forEach(nebTransaction -> {
            JSONObject data = DecodeUtil.decodeData(nebTransaction.getData());
            if (DecodeUtil.isContractTransfer(data)) {
                //将to的合约地址放到对应的contractAddress字段里
                nebTransaction.setContractAddress(nebTransaction.getTo());
                JSONArray args = data.getJSONArray("Args");
                //"Args" -> "["n1JdmmyhrrqBuESseZSbrBucnvugSewSMTE","9299123456789987654321"]"
                String to = args.get(0).toString();
                String value = args.get(1).toString();
                nebTransaction.setTo(to);
                nebTransaction.setValue(value);

                if (nebTransaction.getFrom().equals(addressHash) || nebTransaction.getTo().equals(addressHash)) {
                    Nrc20TransactionVo nrc20TransactionVo = new Nrc20TransactionVo();
                    nrc20TransactionVo.buildFromNebTransaction(nebTransaction);
                    nrc20TransactionVo.setTokenDecimals(contractToken.getTokenDecimals());
                    nrc20TxList.add(nrc20TransactionVo);
                }
            }
        });
        Collections.sort(nrc20TxList, Comparator.comparing(Nrc20TransactionVo::getTimestamp).reversed());
        return nrc20TxList;
    }

}
