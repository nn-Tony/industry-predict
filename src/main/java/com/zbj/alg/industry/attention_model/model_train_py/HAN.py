#coding=utf8

import tensorflow as tf
from tensorflow.contrib import rnn
from tensorflow.contrib import layers

def length(sequences):
#返回一个序列中每个元素的长度
    used = tf.sign(tf.reduce_max(tf.abs(sequences), reduction_indices=2))
    seq_len = tf.reduce_sum(used, reduction_indices=1)
    return tf.cast(seq_len, tf.int32)

class HAN():

    def __init__(self, vocab_size, num_classes, dataset=None, mode='train',  embedding_size=200, hidden_size=50):

        self.vocab_size = vocab_size
        self.num_classes = num_classes
        self.embedding_size = embedding_size
        self.hidden_size = hidden_size
        self.mode = mode
        self.dataset = dataset

        self.weight_initializer = tf.contrib.layers.xavier_initializer()
        self.emb_initializer = tf.random_uniform_initializer(minval=-1.0, maxval=1.0)

        if self.mode == 'train' and self.dataset != None:            
            self.dataset_iterator = self.dataset.make_initializable_iterator()      
            self.input_y, self.input_x = self.dataset_iterator.get_next()
        else:           
            # x的shape为[batch_size, max_time]，但是每个样本的数据都不一样，，所以这里指定为空
            # y的shape为[batch_size, num_classes]
            self.input_x = tf.placeholder(tf.int32, [None, None], name='input_x')
            self.input_y = tf.placeholder(tf.float32, [None, num_classes], name='input_y')

        #构建模型
        word_embedded = self.word2vec()
        sent_vec = self.sent2vec(word_embedded)
        out = self.classifer(sent_vec)

        self.out = out


    def word2vec(self):
        #嵌入层
        with tf.name_scope("embedding"):
            embedding_mat = tf.get_variable('w', [self.vocab_size, self.embedding_size], initializer=self.emb_initializer)
            #shape为[batch_size, max_time, embedding_size]
            word_embedded = tf.nn.embedding_lookup(embedding_mat, self.input_x)
        return word_embedded

    def sent2vec(self, word_embedded):
        with tf.name_scope("sent2vec"):

            #shape为[batch_size, max_time, hidden_size*2]
            word_encoded = self.BidirectionalGRUEncoder(word_embedded, name='word_encoder')
            #shape为[batch_size, hidden_size*2]
            sent_vec = self.AttentionLayer(word_encoded, name='word_attention')
            return sent_vec


    def classifer(self, doc_vec):
        with tf.name_scope('doc_classification'):
            out = layers.fully_connected(inputs=doc_vec, num_outputs=self.num_classes, activation_fn=None,weights_regularizer=layers.l2_regularizer(0.005))
            return out

    def BidirectionalGRUEncoder(self, inputs, name):
        GRU_cell_fw = rnn.GRUCell(self.hidden_size)
        GRU_cell_bw = rnn.GRUCell(self.hidden_size)
        GRU_cell_fw = tf.contrib.rnn.DropoutWrapper(GRU_cell_fw, input_keep_prob=0.9, output_keep_prob=0.9)
        GRU_cell_bw = tf.contrib.rnn.DropoutWrapper(GRU_cell_bw, input_keep_prob=0.9, output_keep_prob=0.9)
        #[batch_size, max_time, voc_size]
        with tf.variable_scope(name) as gru_scope:
            # gru_scope.reuse_variables()
            #fw_outputs和bw_outputs的size都是[batch_size, max_time, hidden_size]
            ((fw_outputs, bw_outputs), (_, _)) = tf.nn.bidirectional_dynamic_rnn(cell_fw=GRU_cell_fw,
                                                                                 cell_bw=GRU_cell_bw,
                                                                                 inputs=inputs,
                                                                                 sequence_length=length(inputs),
                                                                                 dtype=tf.float32)
            #[batch_size, max_time, hidden_size*2]
            outputs = tf.concat((fw_outputs, bw_outputs), 2)
            return outputs

    def AttentionLayer(self, inputs, name):
        #inputs是GRU的输出，size是[batch_size, max_time, encoder_size(hidden_size * 2)]
        with tf.variable_scope(name):
            # u_context是上下文的重要性向量，用于区分不同单词/句子对于句子/文档的重要程度,
            # 因为使用双向GRU，所以其长度为2×hidden_szie
            u_context = tf.get_variable('u_context', [self.hidden_size * 2], initializer=self.weight_initializer)
            
            h = layers.fully_connected(inputs, self.hidden_size * 2, activation_fn=tf.nn.tanh)
            #shape为[batch_size, max_time, 1]
            alpha = tf.nn.softmax(tf.reduce_sum(tf.multiply(h, u_context), axis=2, keep_dims=True), dim=1)
            #before reduce_sum[batch_szie, max_time, hidden_szie*2]，after [batch_size, hidden_size*2]
            atten_output = tf.reduce_sum(tf.multiply(inputs, alpha), axis=1)
            return atten_output