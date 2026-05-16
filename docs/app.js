(function () {
  "use strict";

  var root = document.documentElement;
  var toggle = document.getElementById("theme-toggle");
  var storageKey = "g11n4j-docs-theme";

  function setTheme(theme) {
    if (theme === "dark" || theme === "light") {
      root.setAttribute("data-theme", theme);
      localStorage.setItem(storageKey, theme);
      toggle.textContent = theme === "dark" ? "Light" : "Dark";
    } else {
      root.removeAttribute("data-theme");
      localStorage.removeItem(storageKey);
      toggle.textContent = "Theme";
    }
  }

  var savedTheme = localStorage.getItem(storageKey);
  if (savedTheme) {
    setTheme(savedTheme);
  }

  toggle.addEventListener("click", function () {
    var current = root.getAttribute("data-theme");
    if (current === "dark") {
      setTheme("light");
    } else if (current === "light") {
      setTheme(null);
    } else {
      setTheme("dark");
    }
  });

  document.querySelectorAll("pre > code").forEach(function (code) {
    var pre = code.parentElement;
    var button = document.createElement("button");
    button.type = "button";
    button.className = "copy-button";
    button.textContent = "Copy";
    button.addEventListener("click", function () {
      navigator.clipboard.writeText(code.textContent || "").then(function () {
        button.textContent = "Copied";
        setTimeout(function () {
          button.textContent = "Copy";
        }, 1300);
      }).catch(function () {
        button.textContent = "Failed";
        setTimeout(function () {
          button.textContent = "Copy";
        }, 1300);
      });
    });
    pre.appendChild(button);
  });

  var sectionLinks = Array.from(document.querySelectorAll(".sidebar a"));
  var sectionMap = new Map();
  sectionLinks.forEach(function (link) {
    var id = link.getAttribute("href");
    if (id && id.startsWith("#")) {
      var section = document.querySelector(id);
      if (section) {
        sectionMap.set(section, link);
      }
    }
  });

  var observer = new IntersectionObserver(function (entries) {
    entries.forEach(function (entry) {
      if (entry.isIntersecting) {
        sectionLinks.forEach(function (link) {
          link.classList.remove("active");
        });
        var active = sectionMap.get(entry.target);
        if (active) {
          active.classList.add("active");
        }
      }
    });
  }, {
    rootMargin: "-35% 0px -55% 0px",
    threshold: 0
  });

  sectionMap.forEach(function (_, section) {
    observer.observe(section);
  });
})();
