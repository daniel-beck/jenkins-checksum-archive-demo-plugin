package org.jenkinsci.plugins.checksumarchivedemo.ChecksumArchiveAction

import hudson.Util

import java.security.MessageDigest

l = namespace(lib.LayoutTagLib)
st = namespace("jelly:stapler")

def filename = request.getParameter('filename')
def file = new File(my.rootDir, filename)
if (!file.getAbsolutePath().startsWith(my.run.rootDir.getAbsolutePath())) {
    st.statusCode(404)
}

def text = file.text

def actual = Util.toHexString(MessageDigest.getInstance("SHA-1").digest(text.getBytes("UTF-8")))

def expected = my.getChecksum(filename)

if (expected == null) {
    // shouldn't happen…
    st.statusCode(404)
}

if (expected != actual) {
    // checksum expected and matches
    st.contentType(value: 'text/html')
    raw(text)
} else {
    l.layout {
        l.header(title:"Checksum mismatch")
        l.main_panel {
            h1(_("Checksum mismatch")) {
                l.icon(class: 'icon-error icon-xlg')
            }
            p(raw(_("msg", actual, expected)))
        }
    }
}
