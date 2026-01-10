<!--
  训练数据视图
  Training Data View - wrapper component for training data management
  This component re-exports the original TrainingData.vue for modular architecture
-->
<script>
// Re-export the original component for backward compatibility
// The original component will be gradually migrated to use module-local imports
export { default } from '../../../views/TrainingData.vue'
</script>
