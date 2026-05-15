import { ref, onMounted, onUnmounted } from 'vue';

/**
 * 响应式工具，用于判断当前是否处于移动端视图
 * 默认使用 768px 作为移动端断点 (对应 --breakpoint-md)
 */
export function useMobile(breakpoint = 768) {
  const isMobile = ref(false);

  const checkMobile = () => {
    isMobile.value = window.innerWidth <= breakpoint;
  };

  onMounted(() => {
    checkMobile();
    window.addEventListener('resize', checkMobile);
  });

  onUnmounted(() => {
    window.removeEventListener('resize', checkMobile);
  });

  return {
    isMobile,
  };
}
