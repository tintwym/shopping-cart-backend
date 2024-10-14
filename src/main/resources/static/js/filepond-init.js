// Register the plugins for FilePond
FilePond.registerPlugin(FilePondPluginImagePreview);

// Turn all file input elements into ponds
const ponds = document.querySelectorAll('.filepond');
ponds.forEach(pond => FilePond.create(pond));

// Optional settings for FilePond
FilePond.setOptions({
  server: {
    process: '/products/store',  // Set this to the same URL as the form action
    revert: '/file/revert',      // The route to handle file revert
  },
  maxFileSize: '3MB', // Limit file size
  maxFiles: 3         // Limit number of files
});
