/* static/js/app.js */
document.addEventListener('DOMContentLoaded', () => {
    
    // 1. Theme Toggler
    const themeToggleBtn = document.getElementById('themeToggle');
    const themeIcon = document.getElementById('themeIcon');
    
    if (themeToggleBtn) {
        const currentTheme = document.documentElement.getAttribute('data-bs-theme');
        updateThemeIcon(currentTheme);
        
        themeToggleBtn.addEventListener('click', () => {
            const theme = document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-bs-theme', theme);
            localStorage.setItem('theme', theme);
            updateThemeIcon(theme);
        });
    }

    function updateThemeIcon(theme) {
        if (!themeIcon) return;
        if (theme === 'dark') {
            themeIcon.classList.remove('fa-moon');
            themeIcon.classList.add('fa-sun');
        } else {
            themeIcon.classList.remove('fa-sun');
            themeIcon.classList.add('fa-moon');
        }
    }

    // 2. Navbar Scroll Effect (if transparent navbar is used on home)
    const navbar = document.querySelector('.navbar.fixed-top');
    if (navbar && document.querySelector('.hero-section')) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 50) {
                navbar.classList.remove('navbar-transparent', 'navbar-dark');
                navbar.classList.add('navbar-solid');
                if (document.documentElement.getAttribute('data-bs-theme') !== 'dark') {
                    navbar.classList.add('navbar-light');
                }
            } else {
                navbar.classList.add('navbar-transparent', 'navbar-dark');
                navbar.classList.remove('navbar-solid', 'navbar-light');
            }
        });
        // Trigger on load
        window.dispatchEvent(new Event('scroll'));
    }

    // 3. Initialize AOS Animations
    if (typeof AOS !== 'undefined') {
        AOS.init({
            duration: 800,
            once: true,
            offset: 50,
            disable: window.matchMedia('(prefers-reduced-motion: reduce)').matches
        });
    }

    // 4. Global Form Submit Interceptor (Prevent double submit + loader)
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const btn = this.querySelector('button[type="submit"]');
            if (btn && !this.classList.contains('no-loader')) {
                // Check if form is valid before disabling (if using HTML5 validation)
                if (this.checkValidity()) {
                    const originalText = btn.innerHTML;
                    btn.disabled = true;
                    btn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Loading...`;
                    // Submit naturally
                } else {
                    this.classList.add('shake');
                    setTimeout(() => this.classList.remove('shake'), 500);
                }
            }
        });
    });

    // 5. Initialize Bootstrap Toasts
    const toastElList = [].slice.call(document.querySelectorAll('.toast'));
    const toastList = toastElList.map(function (toastEl) {
        return new bootstrap.Toast(toastEl, { delay: 5000 });
    });
    toastList.forEach(toast => toast.show());

    // 6. Password Visibility & Strength
    const togglePassword = document.querySelector('#togglePassword');
    const passwordInput = document.querySelector('input[type="password"]');
    if (togglePassword && passwordInput) {
        togglePassword.addEventListener('click', function () {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            this.querySelector('i').classList.toggle('fa-eye');
            this.querySelector('i').classList.toggle('fa-eye-slash');
        });
    }

    const strengthBar = document.getElementById('passwordStrength');
    if (strengthBar && passwordInput) {
        passwordInput.addEventListener('input', function() {
            const val = passwordInput.value;
            let strength = 0;
            if (val.length > 5) strength += 25;
            if (val.length > 8) strength += 25;
            if (/[A-Z]/.test(val)) strength += 25;
            if (/[0-9]/.test(val) || /[^A-Za-z0-9]/.test(val)) strength += 25;
            
            strengthBar.style.width = strength + '%';
            if (strength < 50) strengthBar.className = 'progress-bar bg-danger';
            else if (strength < 75) strengthBar.className = 'progress-bar bg-warning';
            else strengthBar.className = 'progress-bar bg-success';
        });
    }

    // 7. Animated Counters
    const counters = document.querySelectorAll('.count-up');
    if (counters.length > 0 && typeof IntersectionObserver !== 'undefined') {
        const observer = new IntersectionObserver(entries => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const target = entry.target;
                    const finalVal = parseInt(target.getAttribute('data-count'));
                    let currentVal = 0;
                    const step = Math.ceil(finalVal / 50);
                    const timer = setInterval(() => {
                        currentVal += step;
                        if (currentVal >= finalVal) {
                            target.innerText = finalVal;
                            clearInterval(timer);
                        } else {
                            target.innerText = currentVal;
                        }
                    }, 30);
                    observer.unobserve(target);
                }
            });
        });
        counters.forEach(c => observer.observe(c));
    }
});
