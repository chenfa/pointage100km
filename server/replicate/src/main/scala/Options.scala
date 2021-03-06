import scopt.OptionParser

class Options(name: String) extends OptionParser(name) {

  private var _compact: Boolean = true
  private var _fixConflicts: Boolean = false
  private var _fixIncomplete: Boolean = false
  private var _obsolete: Boolean = true
  private var _replicate: Boolean = true
  private var _siteId: Int = _
  private var _watchdog: Boolean = true

  def compact: Boolean = _compact
  def fixConflicts: Boolean = _fixConflicts && !isSlave
  def fixIncomplete: Boolean = _fixIncomplete && !isSlave
  def obsolete: Boolean = _obsolete && !isSlave
  def replicate: Boolean = _replicate
  def siteId: Int = _siteId
  def watchdog: Boolean = _watchdog && !isSlave

  def onChanges = fixConflicts || fixIncomplete || watchdog
  def systematic = compact || replicate
  def initOnly = !onChanges && !systematic

  def isSlave = _siteId == 999

  opt("c", "conflicts", "fix conflicts as they appear", { _fixConflicts = true })
  opt("f", "full", "turn on every service", {
    _compact = true
    _fixConflicts = true
    _fixIncomplete = true
    _obsolete = true
    _replicate = true
    _watchdog = true
  })
  help("h", "help", "show this help")
  opt("i", "init-only", "turn off every service", {
    _compact = false
    _fixConflicts = false
    _fixIncomplete = false
    _obsolete = false
    _replicate = false
    _watchdog = false
  })
  opt("I", "incomplete", "fix incomplete checkpoints", { _fixIncomplete = true })
  opt("nc", "no-compact", "compact database regularly", { _compact = false })
  opt("no", "no-obsolete", "do not remove obsolete documents", { _obsolete = false })
  opt("nr", "no-replicate", "start replication", { _replicate = false })
  opt("nw", "no-watchdog", "do not start the watchdog", { _watchdog = false })
  arg("site_id", "numerical id of the current site (999 for slave mode)", {
    s: String => _siteId = Integer.parseInt(s)
  })

}
