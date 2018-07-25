#coding=utf-8
import os
import tensorflow as tf
import time

from com.zbj.alg.industry.attention_model.model_train_py import HAN

# Data loading params
tf.flags.DEFINE_string("data_dir", './data/tmp/train_order_category.tfrecords', "data directory")
tf.flags.DEFINE_integer("vocab_size", 74479, "vocabulary size")
tf.flags.DEFINE_integer("num_classes", 943, "number of classes")
tf.flags.DEFINE_integer("embedding_size", 256, "Dimensionality of character embedding (default: 200)")
tf.flags.DEFINE_integer("hidden_size", 256, "Dimensionality of GRU hidden layer (default: 50)")
tf.flags.DEFINE_integer("batch_size", 256, "Batch Size (default: 64)")
tf.flags.DEFINE_integer("num_epochs", 10, "Number of training epochs (default: 50)")
tf.flags.DEFINE_integer("checkpoint_every", 100, "Save model after this many steps (default: 100)")
tf.flags.DEFINE_integer("num_checkpoints", 5, "Number of checkpoints to store (default: 5)")
tf.flags.DEFINE_integer("evaluate_every", 100, "evaluate every this many batches")
tf.flags.DEFINE_float("learning_rate", 0.001, "learning rate")
tf.flags.DEFINE_float("grad_clip", 5.0, "grad clip to prevent gradient explode")

FLAGS = tf.flags.FLAGS

def _parse_sequence_example(proto):
    context_features = {
        "category_one": tf.FixedLenFeature([], dtype=tf.int64),
        "category_two": tf.FixedLenFeature([], dtype=tf.int64),
        "category_three": tf.FixedLenFeature([], dtype=tf.int64)

    }
    sequence_features = {
        "tokens": tf.FixedLenSequenceFeature([], dtype=tf.int64)
    }

    context_parsed, sequence_parsed = tf.parse_single_sequence_example(
        serialized=proto,
        context_features=context_features,
        sequence_features=sequence_features
    )

    one_hot_category = tf.one_hot(context_parsed['category_three'], FLAGS.num_classes)
    
    return one_hot_category, sequence_parsed['tokens']

filenames = [FLAGS.data_dir]
trainset = tf.contrib.data.TFRecordDataset(filenames)
trainset = trainset.map(_parse_sequence_example)
trainset = trainset.shuffle(buffer_size = 100000)
trainset = trainset.padded_batch(FLAGS.batch_size, padded_shapes=([FLAGS.num_classes], [None]))
trainset = trainset.repeat(FLAGS.num_epochs)

print("data load finished")

with tf.Session() as sess:
    han = HAN.HAN(vocab_size=FLAGS.vocab_size,
                  dataset=trainset,
                  num_classes=FLAGS.num_classes,
                  embedding_size=FLAGS.embedding_size,
                  hidden_size=FLAGS.hidden_size)

    with tf.name_scope('loss'):
        loss = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(labels=han.input_y,
                                                                      logits=han.out,
                                                                      name='loss'))
    with tf.name_scope('accuracy'):
        predict = tf.argmax(han.out, axis=1, name='predict')
        label = tf.argmax(han.input_y, axis=1, name='label')
        acc = tf.reduce_mean(tf.cast(tf.equal(predict, label), tf.float32))
        acc_topk = tf.reduce_mean(tf.cast(tf.nn.in_top_k(han.out, label, 5), tf.float32))

    timestamp = str(int(time.time()))
    out_dir = os.path.abspath(os.path.join(os.path.curdir, "runs", timestamp))
    print("Writing to {}\n".format(out_dir))

    global_step = tf.Variable(0, trainable=False)
    learning_rate = tf.constant(0.001)

    def _learning_rate_decay_fn(learning_rate, global_step):
        return tf.train.exponential_decay(
            learning_rate,
            global_step,
            decay_steps=3000,
            decay_rate=0.9,
            staircase=True)

    learning_rate_decay_fn = _learning_rate_decay_fn

    # Set up the training ops.
    train_op = tf.contrib.layers.optimize_loss(
        loss=loss,
        global_step=global_step,
        learning_rate=learning_rate,
        optimizer='Adam',
        clip_gradients=FLAGS.grad_clip,
        learning_rate_decay_fn=learning_rate_decay_fn)


    checkpoint_dir = os.path.abspath(os.path.join(out_dir, "checkpoints"))
    checkpoint_prefix = os.path.join(checkpoint_dir, "model")
    if not os.path.exists(checkpoint_dir):
        os.makedirs(checkpoint_dir)
    saver = tf.train.Saver(tf.global_variables(), max_to_keep=FLAGS.num_checkpoints)

    sess.run(tf.global_variables_initializer())
    sess.run(han.dataset_iterator.initializer)
    def train_step():
        _, step, cost, accuracy, topK_accuracy = sess.run([train_op, global_step, loss, acc, acc_topk])
        time_str = str(int(time.time()))
        print("{}: step {}, loss {:g}, acc {:g}, topk {:g}".format(time_str, step, cost, accuracy, topK_accuracy))
       
        return step

    def dev_step(x_batch, y_batch):
        feed_dict = {
            han.input_x: x_batch,
            han.input_y: y_batch,

        }
        step, cost, accuracy, topK_accuracy = sess.run([global_step, loss, acc, acc_topk], feed_dict)
        time_str = str(int(time.time()))
        print("++++++++++++++++++dev++++++++++++++{}: step {}, loss {:g}, acc {:g}, topk {:g}".format(time_str, step, cost, accuracy, topK_accuracy))


    while True:
        try:
            step = train_step()
            # if step % FLAGS.evaluate_every == 0:
            #     dev_step(dev_x, dev_y)
            if step % FLAGS.checkpoint_every == 0:
                saver.save(sess, checkpoint_dir, step)
        except tf.errors.OutOfRangeError:
            break