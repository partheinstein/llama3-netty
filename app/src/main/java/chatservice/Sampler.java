package chatservice;

@FunctionalInterface
interface Sampler {
  int sampleToken(FloatTensor logits);

  Sampler ARGMAX = FloatTensor::argmax;
}
