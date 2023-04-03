package org.example.community.util;


import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 定义替换符
    private static final String REPLACEMENT = "***";

    // 定义根节点
    private TrieNode rootNode = new TrieNode();

    // 初始化前缀树
    @PostConstruct
    public void init() {
        try (
                // 读取敏感文件内容
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyWords;
            while ((keyWords = reader.readLine()) != null) {
                // 添加敏感词到前缀树
                this.addKeyWord(keyWords);
            }

        } catch (IOException e) {
            logger.error("加载文件失败" + e.getMessage());
        }
    }

    // 将一个敏感词添加到前缀树中
    private void addKeyWord(String keyWord) {
        // 定义一个节点指向根节点
        TrieNode tempNode = rootNode;
        // 遍历每次传进的字符串
        for (int i = 0; i < keyWord.length(); i++) {
            // 读取每个字符
            char ch = keyWord.charAt(i);
            TrieNode subNode = tempNode.getSubNode(ch);
            // 判断叶子节点是否为空
            if (subNode == null) {
                subNode = new TrieNode();
                // 为空则将当前字符添加进去
                tempNode.addSubNode(ch, subNode);
            }
            // 叶子节点不为空,则 tempNode 指向当前叶子节点 (指向下一个节点，进行下一个循环)
            tempNode = subNode;
            // 如果当前敏感词都遍历完成,设置一个关键词结束标志
            if (i == keyWord.length() - 1) {
                tempNode.setKeyWordEnd(true);
            }
        }
    }


    /**
     * 过滤敏感词
     *
     * @param text
     * @return
     */
    public String filter(String text) {
        // 判断输入是否为空
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1
        TrieNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 存放结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);
            // 判断是否为特殊字符: ※a※b※, 如果是直接跳过特殊字符
            if (isSymbol(c)) {
                // 如果指针一指向的是根节点
                if (tempNode == rootNode) {
                    sb.append(c);
                    // 指针二后移
                    begin++;
                }
                // 不管特殊字符在哪个位置,指针三都得移动
                position++;
                // 继续
                continue;
            }

            // 如果当前字符不是特殊字符,tempNode 指向下一节点
            tempNode = tempNode.getSubNode(c);
            // 如果当前节点不是敏感词开头的字符
            if (tempNode == null) {
                // 将该字符添加
                sb.append(c);
                // 指针后移
                position = ++begin;
                // tempNode 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeyWordEnd()) {
                // 发现敏感词,将从begin-position这段区间的字符替换
                sb.append(REPLACEMENT);
                begin = ++position;
                // tempNode 重新指向根节点
                tempNode = rootNode;
            } else {
                // 如果当前字符属于敏感词里面,则继续后移
                if (position < text.length() - 1) {
                    position++;
                } else {
                    // 比如有敏感词:fabc 但是我们的输入的是: bbccfabc
                    sb.append(text.charAt(begin));
                    position = ++begin;
                    tempNode = rootNode;
                }
            }

        }

        return sb.toString();
    }


    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    // 构造一颗前缀树
    public class TrieNode {
        // 关键词结束标志
        private boolean isKeyWordEnd = false;
        // 子节点(key: 下一级的字符, value: 下一级的节点)
        Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }
    }

}
